#!/usr/bin/env bash

set -e

ENVIRONMENT=${1:-dev}

if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
  echo "Usage: ./scripts/docker-up.sh [dev|prod]"
  exit 1
fi

echo "Starting Docker services for: $ENVIRONMENT"

docker compose \
  -f docker-compose.yml \
  -f "docker-compose-${ENVIRONMENT}.yml" \
  up -d
