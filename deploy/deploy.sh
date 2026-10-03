#!/usr/bin/env bash
# Deploys released versions to the VPS. Run it from your machine:
#
#   deploy/deploy.sh                                        newest version of each app in versions.yml on main
#   API_VERSION=0.0.2 deploy/deploy.sh                      pin the API, newest frontend
#   API_VERSION=0.0.1 FRONTEND_VERSION=0.0.1 deploy/deploy.sh
#   DRY_RUN=1 deploy/deploy.sh                              run every check, change nothing
set -euo pipefail

cd "$(dirname "$0")/.."

SEMVER='^[0-9]+\.[0-9]+\.[0-9]+$'
DEPLOY_FILES=(deploy/compose.yml deploy/remote-deploy.sh)

fail() {
    echo "Error: $1" >&2
    exit 1
}

if [[ ! -f deploy/vps.env ]]; then
    fail "Missing deploy/vps.env - copy deploy/vps.env.example and fill it in."
fi
# shellcheck source=/dev/null
source deploy/vps.env
[[ -n "${VPS_HOST:-}" && -n "${VPS_USER:-}" && -n "${VPS_APP_DIR:-}" ]] \
    || fail "deploy/vps.env must set VPS_HOST, VPS_USER and VPS_APP_DIR."

git fetch --quiet origin main

newest_version() {
    command -v yq > /dev/null || fail "yq is needed to read versions.yml (brew install yq), or set API_VERSION and FRONTEND_VERSION."
    git show origin/main:versions.yml | yq ".${1}[-1].version"
}

# The release workflow creates the tag only after the image is pushed, so a tag means the image exists.
require_released() {
    local app=$1
    local version=$2
    [[ "$version" =~ $SEMVER ]] || fail "${app} version '${version}' is not MAJOR.MINOR.PATCH."
    git ls-remote --exit-code --tags origin "refs/tags/${app}-v${version}" > /dev/null \
        || fail "${app} ${version} has not been released, so it can't be deployed."
}

API_VERSION=${API_VERSION:-$(newest_version api)}
FRONTEND_VERSION=${FRONTEND_VERSION:-$(newest_version frontend)}
require_released api "$API_VERSION"
require_released frontend "$FRONTEND_VERSION"

# Production should only ever run deploy files that were reviewed and merged.
git diff --quiet origin/main -- "${DEPLOY_FILES[@]}" \
    || fail "Your copy of ${DEPLOY_FILES[*]} differs from origin/main. Deploy from an up-to-date main checkout."

remote="${VPS_USER}@${VPS_HOST}"
echo "Deploying to ${remote}:${VPS_APP_DIR}"
echo "  api       ${API_VERSION}"
echo "  frontend  ${FRONTEND_VERSION}"

if [[ "${DRY_RUN:-}" == "1" ]]; then
    echo "Dry run - nothing was changed."
    exit 0
fi

read -r -p "Continue? [y/N] " answer
[[ "$answer" == [yY] ]] || fail "Cancelled."

scp -p "${DEPLOY_FILES[@]}" "${remote}:${VPS_APP_DIR}/"

remote_command=$(printf 'API_VERSION=%q FRONTEND_VERSION=%q %q' \
    "$API_VERSION" "$FRONTEND_VERSION" "${VPS_APP_DIR}/remote-deploy.sh")
# shellcheck disable=SC2029
if ! ssh "$remote" "$remote_command"; then
    echo "Deploy failed and production may now be running the failed versions." >&2
    echo "Run deploy/rollback.sh to return to the last deployment that passed its health checks." >&2
    exit 1
fi
