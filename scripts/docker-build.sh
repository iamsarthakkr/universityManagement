#!/usr/bin/env bash

set -e

ENVIRONMENT=${1:-dev}

if [[ "$ENVIRONMENT" == "dev" ]]; then
  docker compose \
    -f docker-compose.yml \
    -f docker-compose-dev.yml \
    build
elif [[ "$ENVIRONMENT" == "prod" ]]; then
  echo "Production uses the published GHCR image. No local build needed."
else
  echo "Usage: ./scripts/docker-build.sh [dev|prod]"
  exit 1
fi