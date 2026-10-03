#!/usr/bin/env bash
# Runs on the VPS. deploy/deploy.sh uploads it and runs it with API_VERSION and FRONTEND_VERSION set.
set -euo pipefail

cd "$(dirname "$0")"

API_VERSION=${API_VERSION:?API_VERSION is required}
FRONTEND_VERSION=${FRONTEND_VERSION:?FRONTEND_VERSION is required}

HEALTH_TIMEOUT_SECONDS=120
APP_LABEL="tech.imsarthakkr.app=university-management"
IMAGE_RETENTION="168h"
RELEASE_FILE=release.env
HISTORY_FILE=deploy-history

if [[ ! -f .env ]]; then
    echo "Missing $(pwd)/.env - copy .env.example and fill it in." >&2
    exit 1
fi

# release.env pins the image versions, so compose commands run by hand on the VPS use the same ones:
#   docker compose --env-file .env --env-file release.env logs server
compose() {
    docker compose --env-file .env --env-file "$RELEASE_FILE" "$@"
}

wait_until_healthy() {
    local service=$1
    local container_port=$2
    local path=$3

    local address
    if ! address=$(compose port "$service" "$container_port"); then
        echo "$service has no published port $container_port - the container is not running." >&2
        compose logs --tail 50 "$service" >&2
        return 1
    fi

    local url="http://${address}${path}"
    local deadline=$((SECONDS + HEALTH_TIMEOUT_SECONDS))

    until curl -fsS --connect-timeout 2 --max-time 5 -o /dev/null "$url"; do
        if ((SECONDS >= deadline)); then
            echo "$service did not become healthy at $url within ${HEALTH_TIMEOUT_SECONDS}s" >&2
            compose logs --tail 50 "$service" >&2
            return 1
        fi
        sleep 2
    done

    echo "$service is healthy at $url"
}

printf 'API_VERSION=%s\nFRONTEND_VERSION=%s\n' "$API_VERSION" "$FRONTEND_VERSION" > "$RELEASE_FILE"

compose pull
compose up -d --remove-orphans

wait_until_healthy server 8080 /actuator/health
wait_until_healthy frontend 80 /

# Only deployments that passed the health checks are recorded; deploy/rollback.sh picks its target from here.
echo "$(date -u +%Y-%m-%dT%H:%M:%SZ) api=${API_VERSION} frontend=${FRONTEND_VERSION}" >> "$HISTORY_FILE"

docker image prune -f --filter "label=${APP_LABEL}" --filter "until=${IMAGE_RETENTION}"

echo "Deployed api ${API_VERSION} and frontend ${FRONTEND_VERSION}"
