#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

HEALTH_TIMEOUT_SECONDS=120
APP_LABEL="tech.imsarthakkr.app=university-management"
IMAGE_RETENTION="168h"

if [[ ! -f .env ]]; then
    echo "Missing $(pwd)/.env - copy .env.example and fill it in." >&2
    exit 1
fi

wait_until_healthy() {
    local service=$1
    local container_port=$2
    local path=$3

    local address
    if ! address=$(docker compose port "$service" "$container_port"); then
        echo "$service has no published port $container_port - the container is not running." >&2
        docker compose logs --tail 50 "$service" >&2
        return 1
    fi

    local url="http://${address}${path}"
    local deadline=$((SECONDS + HEALTH_TIMEOUT_SECONDS))

    until curl -fsS --connect-timeout 2 --max-time 5 -o /dev/null "$url"; do
        if ((SECONDS >= deadline)); then
            echo "$service did not become healthy at $url within ${HEALTH_TIMEOUT_SECONDS}s" >&2
            docker compose logs --tail 50 "$service" >&2
            return 1
        fi
        sleep 2
    done

    echo "$service is healthy at $url"
}

docker compose pull
docker compose up -d --remove-orphans

wait_until_healthy server 8080 /actuator/health
wait_until_healthy frontend 80 /

docker image prune -f --filter "label=${APP_LABEL}" --filter "until=${IMAGE_RETENTION}"
