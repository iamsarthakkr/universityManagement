#!/usr/bin/env bash
# Rolls production back to the most recent deployment that passed its health checks and differs from what is
# running now, using deploy/deploy.sh. Running it twice undoes the rollback; to go further back, pin versions:
#   API_VERSION=0.0.1 FRONTEND_VERSION=0.0.1 deploy/deploy.sh
#
#   deploy/rollback.sh
#   DRY_RUN=1 deploy/rollback.sh                            show the target, change nothing
set -euo pipefail

cd "$(dirname "$0")/.."

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

remote="${VPS_USER}@${VPS_HOST}"
app_dir=$(printf '%q' "$VPS_APP_DIR")

# shellcheck disable=SC2029
running=$(ssh "$remote" "cat ${app_dir}/release.env") \
    || fail "Couldn't read release.env on the VPS - nothing has been deployed with deploy/deploy.sh yet."
# shellcheck disable=SC2029
history=$(ssh "$remote" "cat ${app_dir}/deploy-history 2>/dev/null || true")

running_api=$(sed -n 's/^API_VERSION=//p' <<< "$running")
running_frontend=$(sed -n 's/^FRONTEND_VERSION=//p' <<< "$running")

# History lines look like "2026-10-04T10:00:00Z api=0.0.2 frontend=0.0.1"; keep the newest that differs.
target=$(awk -v running="api=${running_api} frontend=${running_frontend}" \
    '$2 " " $3 != running { target = $0 } END { print target }' <<< "$history")
[[ -n "$target" ]] || fail "No earlier deployment that passed its health checks is recorded - nothing to roll back to."

read -r deployed_at api frontend <<< "$target"
echo "Running now:     api ${running_api}, frontend ${running_frontend}"
echo "Rolling back to: api ${api#api=}, frontend ${frontend#frontend=} (deployed ${deployed_at})"

API_VERSION=${api#api=} FRONTEND_VERSION=${frontend#frontend=} exec deploy/deploy.sh
