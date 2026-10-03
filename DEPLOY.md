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
  `ghcr.io/iamsarthakkr/university-management-frontend`, tagged with their version (`0.0.1`, ...).
- Each app is versioned on its own in [`versions.yml`](versions.yml). GitHub builds and releases new versions; you
  deploy them from your machine (see [Releasing and deploying](#releasing-and-deploying)).

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

## Releasing and deploying

GitHub builds the images; you decide when production changes.

```text
PR ──► CI ──► merge to main ──► CI ──► Release ──► image <app>:<version> on GHCR + GitHub Release <app>-v<version>

your machine: deploy/deploy.sh ──ssh──► VPS: remote-deploy.sh (pull, start, health-check)
```

| Workflow | Runs on | Does |
| --- | --- | --- |
| [`ci.yml`](.github/workflows/ci.yml) (CI) | every PR and push to `main`, except changes only to docs and dev-only files | `versions.yml` validated and checked against `pom.xml` / `package.json`; backend tests (`./mvnw -B verify`) and a backend image build (no push) if `server/**` changed; frontend lint, tests with coverage thresholds, build and image build (no push) if `frontend/**` changed; `compose.yml` validated against `.env.example` and the `deploy/` scripts shellchecked if `deploy/**` changed |
| [`release.yml`](.github/workflows/release.yml) (Release) | CI succeeding on a push to `main` | for each app whose newest version in `versions.yml` has no GitHub Release yet: builds the image from the commit CI tested, pushes it as `<version>`, then creates the GitHub Release `<app>-v<version>` |

- Nothing in GitHub touches the VPS. There are no deploy secrets in the repository.
- Images are only ever tagged with their version — there is no `latest`. Production always runs an explicit
  version.
- The GitHub Release is created after the image is pushed, so a release (and its tag) means the image exists.
  If a release fails before that, the next CI-passing push to `main` builds that version again — from the newer
  commit. To rebuild it from the original commit, use **Re-run failed jobs** on the failed Release run.
- Pushes that only touch Markdown, `LICENSE`, `.gitignore` files, `compose.dev.yml`, the dev profile or
  `deploy/Caddyfile.example` don't run CI and therefore never release (`paths-ignore` in `ci.yml`).
- Release is triggered by the workflow named `CI` — keep that `name:` in `ci.yml` in sync.

### Releasing a version

A release is a new entry at the end of an app's list in [`versions.yml`](versions.yml), together with the same
version in that app's build file:

```yaml
api:
  - version: 0.0.1
    notes: Initial release
  - version: 0.0.2                  # new release
    notes: Short description of what changed
```

| App | Build file to bump |
| --- | --- |
| `api` | `server/pom.xml` (`<version>`) |
| `frontend` | `frontend/package.json` (`"version"`), then `npm install --package-lock-only` in `frontend/` |

CI's **Versions file** job (`.github/scripts/versions.sh validate`) fails the PR unless every version is
`MAJOR.MINOR.PATCH`, strictly higher than the one before it, has `notes`, and the newest version of each app matches
its build file. Once merged, Release publishes it. A released version is never rebuilt or overwritten.

### Deploying

From an up-to-date `main` checkout on your machine:

```bash
deploy/deploy.sh                                          # newest version of each app in versions.yml on main
API_VERSION=0.0.2 deploy/deploy.sh                        # pin the API, newest frontend
API_VERSION=0.0.1 FRONTEND_VERSION=0.0.1 deploy/deploy.sh # pin both (also how you go back further than a rollback)
DRY_RUN=1 deploy/deploy.sh                                # run every check, change nothing
```

The script refuses to continue unless:

- each version has a GitHub Release (so its image exists);
- your `deploy/compose.yml` and `deploy/remote-deploy.sh` match `origin/main` — production only runs merged files.

It then asks for confirmation, uploads those two files to the VPS and runs `remote-deploy.sh` there, which:

1. writes the versions to `release.env` on the VPS;
2. pulls the images and restarts what changed;
3. waits up to 120 seconds per service for the API health endpoint and the frontend to respond (each request times
   out after 5 seconds), and on failure prints the service's logs and stops;
4. on success, appends the deployment to `deploy-history` on the VPS and prunes this app's dangling images.

A failed health check leaves the new containers running (and failing) — roll back.

### Rolling back

```bash
deploy/rollback.sh             # back to the last deployment that passed its health checks
DRY_RUN=1 deploy/rollback.sh   # show what it would roll back to
```

It reads what's running (`release.env`) and the history (`deploy-history`) from the VPS, picks the newest recorded
deployment whose versions differ from what's running, and hands those versions to `deploy/deploy.sh`. So:

- after a deploy that **failed** its health checks, it returns to the last working one;
- after a deploy that **succeeded** but is broken, it returns to the one before it;
- running it again undoes the rollback. To go further back, pin versions with `deploy/deploy.sh`.

### On the VPS

Because `compose.yml` requires the versions, compose commands run by hand need `release.env` too:

```bash
cd /srv/apps/university-management
docker compose --env-file .env --env-file release.env logs server
docker compose --env-file .env --env-file release.env ps
```

The running versions are also visible at `GET /api/actuator/info` (API) and at the bottom of the user menu (frontend).

## One-time setup

1. **Database** — on the shared MySQL, create the app's database and a user limited to it. The user name and password
   must match `DB_USERNAME` / `DB_PASSWORD` in the VPS `.env`:
   ```sql
   CREATE DATABASE university;
   CREATE USER 'university_management_user'@'%' IDENTIFIED BY '<password>';
   GRANT ALL PRIVILEGES ON university.* TO 'university_management_user'@'%';
   ```
   Flyway creates the schema on the API's first start. If the API logs `Access denied ... to database` (MySQL error
   1044), the user exists but is missing this `GRANT`.
2. **App directory** — create `/srv/apps/university-management` on the VPS and put a `.env` there based on
   [`deploy/.env.example`](deploy/.env.example) (`DB_URL=jdbc:mysql://mysql:3306/university`, credentials, admin
   account, `JWT_SECRET` of at least 32 characters). `deploy/deploy.sh` uploads `compose.yml` and `remote-deploy.sh`;
   `.env` is never touched. The VPS user needs permission to run `docker`.
3. **Caddy** — add the site block from [`deploy/Caddyfile.example`](deploy/Caddyfile.example) to the global Caddyfile
   (with your domain) and reload Caddy. If you change `API_HOST_PORT` / `FRONTEND_HOST_PORT` in `.env`, use the same
   ports there.
4. **Your machine** — copy [`deploy/vps.env.example`](deploy/vps.env.example) to `deploy/vps.env` (git-ignored) and
   set `VPS_HOST`, `VPS_USER` and `VPS_APP_DIR`. SSH uses your own keys and `~/.ssh/config`, so make sure
   `ssh <user>@<host>` works without a password prompt. Install `yq` (`brew install yq`) to have `deploy/deploy.sh`
   default to the newest versions; without it, set both `API_VERSION` and `FRONTEND_VERSION`.

## Image cleanup

Both images carry the label `tech.imsarthakkr.app=university-management`. After a successful deploy,
`remote-deploy.sh` removes only dangling images with that label that are older than 7 days, so other apps' images on
the shared VPS are never touched. Images of previous versions stay tagged and are kept, which makes rollbacks fast. A
failed deploy cleans up nothing.

## Files in `deploy/`

| File | Purpose | Where it runs |
| --- | --- | --- |
| `deploy.sh` | check versions and files, upload, run `remote-deploy.sh` | your machine |
| `rollback.sh` | find the previous working deployment and deploy it with `deploy.sh` | your machine |
| `vps.env.example` | template for `deploy/vps.env` — how to reach the VPS | your machine |
| `compose.yml` | the API and frontend services, ports, shared `database` network | VPS, uploaded by every deploy |
| `remote-deploy.sh` | pull, restart, health-check, record history, prune this app's old images | VPS, uploaded by every deploy |
| `.env.example` | template for the VPS `.env` | VPS, you create `.env` from it once |
| `Caddyfile.example` | this app's site block for the global Caddyfile | VPS, copied into the Caddyfile by hand |

Files the scripts create on the VPS: `release.env` (the versions currently deployed) and `deploy-history` (one line
per deployment that passed its health checks).
