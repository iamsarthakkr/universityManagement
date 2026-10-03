#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
    echo "Missing $(pwd)/.env - copy .env.example and fill it in." >&2
    exit 1
fi

wait_until_healthy() {
    local service=$1
    local container_port=$2
    local path=$3

    local address
    address=$(docker compose port "$service" "$container_port")

    for _ in $(seq 1 60); do
        if curl -fsS -o /dev/null "http://${address}${path}"; then
            echo "$service is healthy at http://${address}${path}"
            return 0
        fi
        sleep 2
    done

    echo "$service did not become healthy at http://${address}${path}" >&2
    docker compose logs --tail 50 "$service" >&2
    return 1
}

docker compose pull
docker compose up -d --remove-orphans
docker image prune -f

wait_until_healthy server 8080 /actuator/health
wait_until_healthy frontend 80 /
