# Deployment

How University Enrollment Management runs in production and how changes get there. For what the application does
and how to run it locally, see [README.md](README.md).

## Overview

```text
Browser ──► https://<domain> ──► Caddy (shared, on the VPS host)
                                   ├─ /api/*  ──► 127.0.0.1:8080  API container      ──► mysql:3306 (shared MySQL,
                                   │              (prefix stripped)                       Docker network "database")
                                   └─ /*      ──► 127.0.0.1:3000  frontend container
```

- The VPS hosts several apps behind one global **Caddyfile** and one shared **MySQL** container (service `mysql` on
  the Docker network `database`).
- This repo deploys only its two containers, defined in [`deploy/compose.yml`](deploy/compose.yml). Both listen on
  `127.0.0.1` only, so they're reachable through Caddy and nowhere else.
- Images are published to GHCR (public): `ghcr.io/iamsarthakkr/university-management-api` and
  `ghcr.io/iamsarthakkr/university-management-frontend`.

## How a request is served

The app and the API share one origin. The frontend calls relative `/api/...` URLs, and Caddy routes them:

- `/api/*` goes to the API with the `/api` prefix stripped (`handle_path`), so `/api/auth/login` reaches the API as
  `/auth/login`.
- Everything else goes to the frontend container.

Because the browser only ever sees one origin, no API URL is baked into the frontend build (the same image works in
every environment) and the API needs no CORS entry for production. Caddy talks to the API over plain HTTP, so the
API trusts Caddy's `X-Forwarded-Proto`/`X-Forwarded-Host` headers (`server.forward-headers-strategy: framework`) to
recognise same-origin requests. That's safe only because the API is bound to `127.0.0.1`.

## Containers

**API** — the Spring Boot jar from `server/Dockerfile`, run with the `prod` profile. Every setting comes from the VPS
`.env` (see [`deploy/.env.example`](deploy/.env.example)); `compose.yml` refuses to start if a required one is
missing. Flyway applies migrations on startup. Health: `/actuator/health`.

**Frontend** — the Vite build served by nginx (`frontend/Dockerfile`, `frontend/nginx.conf`):

- falls back to `index.html` for client-side routes, so deep links like `/dashboard/admin` work on refresh;
- caches hashed `/assets/*` for a year and serves `index.html` with `no-cache`, so new deploys are picked up
  immediately;
- returns 404 for `/api/*` — it knows nothing about the backend; routing is Caddy's job.

## CI/CD

| Workflow | Runs on | Does |
| --- | --- | --- |
| [`ci.yml`](.github/workflows/ci.yml) (CI) | every PR and push to `main`, except changes only to docs and dev-only files | backend tests (`./mvnw -B verify`) and a backend image build (no push) if `server/**` changed; frontend lint, tests with coverage thresholds, build and image build (no push) if `frontend/**` changed; `compose.yml` validated against `.env.example` and `deploy.sh` shellchecked if `deploy/**` changed |
| [`deploy.yml`](.github/workflows/deploy.yml) (Deploy) | CI succeeding on a push to `main`, or a manual run | builds and pushes both images to GHCR (`latest` + the commit sha), then copies `deploy/compose.yml` and `deploy/deploy.sh` to the VPS over SSH and runs `deploy.sh` |

- Failed CI runs and pull requests never deploy.
- Deploy builds from the exact commit CI tested. Unchanged images rebuild from the layer cache with the same digest,
  so their containers aren't restarted.
- `deploy.sh` pulls the images, restarts what changed, prunes old images and fails unless the API health endpoint
  and the frontend respond.
- Deploys are queued, never run in parallel, and never cancelled midway.
- Pushes that only touch Markdown, `LICENSE`, `.gitignore` files, `compose.dev.yml`, the dev profile or
  `deploy/Caddyfile.example` don't run CI and therefore don't deploy (`paths-ignore` in `ci.yml`).
- The deploy workflow is triggered by the workflow named `CI` — keep that `name:` in `ci.yml` in sync.

## One-time setup

1. **Database** — on the shared MySQL, create the app's database and a user limited to it:
   ```sql
   CREATE DATABASE university;
   CREATE USER 'university'@'%' IDENTIFIED BY '<password>';
   GRANT ALL PRIVILEGES ON university.* TO 'university'@'%';
   ```
   Flyway creates the schema on the API's first start.
2. **App directory** — create `/srv/apps/university-management` on the VPS and put a `.env` there based on
   [`deploy/.env.example`](deploy/.env.example) (`DB_URL=jdbc:mysql://mysql:3306/university`, credentials, admin
   account, `JWT_SECRET` of at least 32 characters). The pipeline uploads `compose.yml` and `deploy.sh`; `.env` is
   never touched by CI.
3. **Caddy** — add the site block from [`deploy/Caddyfile.example`](deploy/Caddyfile.example) to the global Caddyfile
   (with your domain) and reload Caddy. If you change `API_HOST_PORT` / `FRONTEND_HOST_PORT` in `.env`, use the same
   ports there.
4. **GitHub** — create an environment named `production` with:
   - secrets: `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY` (private key of a deploy key whose public key is in the VPS
     user's `~/.ssh/authorized_keys`), `VPS_KNOWN_HOSTS` (output of `ssh-keyscan <host>`)
   - variable: `VPS_APP_DIR` = `/srv/apps/university-management`

   The VPS user needs permission to run `docker`.

## Deploying manually and rolling back

Run the **Deploy** workflow from the Actions tab, or on the VPS:

```bash
/srv/apps/university-management/deploy.sh
```

To roll back, set `API_IMAGE_TAG` and `FRONTEND_IMAGE_TAG` in the VPS `.env` to an earlier commit sha (every deploy
tags both images with the commit it was built from) and run `deploy.sh`. Set them back to `latest` to resume normal
deploys.

## Files in `deploy/`

| File | Purpose | On the VPS |
| --- | --- | --- |
| `compose.yml` | the API and frontend services, ports, shared `database` network | uploaded by every deploy |
| `deploy.sh` | pull, restart, prune, health-check | uploaded by every deploy |
| `.env.example` | template for the VPS `.env` | you create `.env` from it once |
| `Caddyfile.example` | this app's site block for the global Caddyfile | copied into the Caddyfile by hand |
