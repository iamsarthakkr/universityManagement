#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

VERSIONS_FILE=versions.yml
SEMVER='^[0-9]+\.[0-9]+\.[0-9]+$'

fail() {
    echo "::error file=${VERSIONS_FILE}::$1" >&2
    exit 1
}

latest_version() {
    yq ".${1}[-1].version" "$VERSIONS_FILE"
}

build_file_version() {
    case "$1" in
        api) yq -p xml -oy '.project.version' server/pom.xml ;;
        frontend) yq -p json -oy '.version' frontend/package.json ;;
    esac
}

validate() {
    local unknown
    unknown=$(yq 'keys | .[]' "$VERSIONS_FILE" | grep -vxE 'api|frontend' || true)
    [[ -z "$unknown" ]] || fail "Unknown app in ${VERSIONS_FILE}: ${unknown}"

    local app
    for app in api frontend; do
        local count
        count=$(yq ".${app} | length" "$VERSIONS_FILE")
        ((count > 0)) || fail "${app} has no versions"

        local previous="" index version notes
        for ((index = 0; index < count; index++)); do
            version=$(yq ".${app}[${index}].version" "$VERSIONS_FILE")
            notes=$(yq ".${app}[${index}].notes // \"\"" "$VERSIONS_FILE")

            [[ "$version" =~ $SEMVER ]] || fail "${app}: '${version}' is not MAJOR.MINOR.PATCH"
            [[ -n "$notes" ]] || fail "${app} ${version}: notes are required"
            if [[ -n "$previous" ]]; then
                local lower
                lower=$(printf '%s\n%s\n' "$previous" "$version" | sort -V | head -n 1)
                [[ "$version" != "$previous" && "$lower" == "$previous" ]] \
                    || fail "${app}: ${version} must be higher than the version before it (${previous})"
            fi
            previous=$version
        done

        local declared
        declared=$(build_file_version "$app")
        [[ "$declared" == "$previous" ]] \
            || fail "${app}: latest version in ${VERSIONS_FILE} is ${previous}, but its build file says ${declared}"

        echo "${app}: ${count} version(s), latest ${previous}"
    done

    local lock_version
    lock_version=$(yq -p json -oy '.version' frontend/package-lock.json)
    [[ "$lock_version" == "$(latest_version frontend)" ]] \
        || fail "frontend/package-lock.json says ${lock_version} - run npm install --package-lock-only in frontend/"
}

case "${1:-}" in
    validate) validate ;;
    latest) latest_version "${2:?usage: versions.sh latest <api|frontend>}" ;;
    *)
        echo "usage: versions.sh validate | latest <api|frontend>" >&2
        exit 1
        ;;
esac
