#!/usr/bin/env bash

set -e

ENVIRONMENT=${1:-dev}

if [[ "$ENVIRONMENT" == "dev" ]]; then
  docker compose \
    -f docker-compose.yml \
    -f docker-compose-dev.yml \
    up -d --build

elif [[ "$ENVIRONMENT" == "prod" ]]; then
  docker compose \
    -f docker-compose.yml \
    -f docker-compose-prod.yml \
    pull

  docker compose \
    -f docker-compose.yml \
    -f docker-compose-prod.yml \
    up -d

else
  echo "Usage: ./scripts/docker-up.sh [dev|prod]"
  exit 1
fi