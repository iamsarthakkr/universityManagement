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
  `ghcr.io/iamsarthakkr/university-management-frontend`, tagged with their version (`0.0.1`, ...) and `latest`.
- Each app is versioned on its own in [`versions.yml`](versions.yml); production always runs exactly the newest
  version of each app listed there (see [Releasing](#releasing)).

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
| [`ci.yml`](.github/workflows/ci.yml) (CI) | every PR and push to `main`, except changes only to docs and dev-only files | `versions.yml` validated and checked against `pom.xml` / `package.json`; backend tests (`./mvnw -B verify`) and a backend image build (no push) if `server/**` changed; frontend lint, tests with coverage thresholds, build and image build (no push) if `frontend/**` changed; `compose.yml` validated against `.env.example` and `deploy.sh` shellchecked if `deploy/**` changed |
| [`deploy.yml`](.github/workflows/deploy.yml) (Deploy) | CI succeeding on a push to `main`, or a manual run | releases every app whose newest version in `versions.yml` has no GitHub Release yet (image `<version>` + `latest`, GitHub Release `<app>-v<version>`), then deploys the newest version of each app; a manual run deploys already-released versions without building |

- Failed CI runs and pull requests never deploy. Manual runs go through the same gate: the `verify` job refuses to
  continue unless the workflow was started from `main`, the commit is on `main`, and CI succeeded for that exact
  commit.
- A push to `main` that doesn't add a new version to `versions.yml` only runs CI — nothing is built or deployed.
- Releases build from the exact commit CI verified. Only the apps with a new version are built; the other app's
  version (and container) stays as it is.
- `deploy.sh` pulls the images, restarts what changed, and fails unless the API health endpoint and the frontend
  respond. Every health request times out after 5 seconds and each service gets 120 seconds in total, after which the
  script prints the service's logs and fails. The deploy job itself is capped at 15 minutes.
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

## Releasing

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
its build file.

When the change reaches `main` and CI passes, Deploy:

1. releases each app whose newest version has no GitHub Release yet — builds its image, pushes `<version>` and
   `latest`, and creates the GitHub Release `<app>-v<version>` (titled `<app> <version>`, with the notes) at that
   commit;
2. deploys the newest version of both apps, pinned explicitly (`API_IMAGE_TAG` / `FRONTEND_IMAGE_TAG` are passed to
   `deploy.sh`, overriding the VPS `.env`).

A released version is never rebuilt or overwritten: the GitHub Release is the record that it exists. If a run fails
after pushing an image but before creating its Release, the next run rebuilds and releases that version.

The running versions are visible at `GET /api/actuator/info` (API) and at the bottom of the user menu (frontend).

## Deploying manually and rolling back

Run the **Deploy** workflow from the Actions tab on the `main` branch. It never builds anything — it deploys versions
that already have a GitHub Release:

| Input | Default | Use |
| --- | --- | --- |
| `api_version` | newest API version in `versions.yml` | roll the API back (or forward) to any released version |
| `frontend_version` | newest frontend version | the same for the frontend |
| `sha` | latest commit on `main` | which commit's `deploy/` files to upload; it must be on `main` and have a successful CI run (a docs-only commit has none, so pass the last CI-verified commit) |

For example, rolling the API back to `0.0.1` while keeping the frontend: run Deploy with `api_version: 0.0.1`. A
rollback lasts until the next automatic deploy: releasing a new version of **either** app deploys the newest version of
both apps from `versions.yml` again. To keep a bad version from coming back, release a fixed one.

On the VPS, `deploy.sh` can also be run directly; it then uses `API_IMAGE_TAG` / `FRONTEND_IMAGE_TAG` from `.env`, or
override them for one run:

```bash
API_IMAGE_TAG=0.0.1 /srv/apps/university-management/deploy.sh
```

## Image cleanup

Both images carry the label `tech.imsarthakkr.app=university-management`. After a successful deploy, `deploy.sh`
removes only dangling images with that label that are older than 7 days, so other apps' images on the shared VPS are
never touched and the last week of this app's previous images stays available locally. A failed deploy cleans up
nothing.

## Files in `deploy/`

| File | Purpose | On the VPS |
| --- | --- | --- |
| `compose.yml` | the API and frontend services, ports, shared `database` network | uploaded by every deploy |
| `deploy.sh` | pull, restart, health-check, prune this app's old images | uploaded by every deploy |
| `.env.example` | template for the VPS `.env` | you create `.env` from it once |
| `Caddyfile.example` | this app's site block for the global Caddyfile | copied into the Caddyfile by hand |
