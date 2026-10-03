# University Enrollment Frontend

Single-page dashboard UI (Vite + React Router) for the Spring Boot University Enrollment Management project.

## Included

- Login page
- Student registration request page
- Instructor registration request page
- Admin dashboard
- Pending student registration table
- Pending instructor registration table
- Dummy student dashboard pages
- Dummy instructor dashboard pages
- Shared dashboard shell, sidebar, buttons, inputs, stat cards, page headers
- Tailwind CSS v4 styling

## Run

```bash
npm install
npm run dev            # http://localhost:3000
npm run build          # type-check + static build into dist/
npm run preview        # serve dist/ locally
npm test               # run tests in watch mode (npm run test:run for a single run)
npm run test:coverage  # run tests with coverage report in coverage/
```

The app calls the API on its own origin under `/api` (e.g. `/api/departments`). `npm run dev` and `npm run preview`
proxy `/api/*` to the backend at `http://localhost:8080`, stripping the `/api` prefix, so start the backend first —
either from your IDE, or with MySQL in Docker from the repo root:

```bash
./scripts/docker-up.sh dev    # MySQL on localhost:3307 + backend on localhost:8080
```

The frontend isn't part of the dev Docker stack; it runs with `npm run dev` for hot reload. Its Docker image is only
used in production (`docker-compose-prod.yml`).

## Deploying

The `Dockerfile` builds the app and serves the static files with nginx (`nginx.conf`). nginx falls back to
`index.html` for client-side routes, so deep links like `/dashboard/admin` work on refresh. It knows nothing about the
backend: `/api/*` returns 404 from this container.

Routing is done by the reverse proxy in front of both containers (Caddy on the VPS), which serves the app and the API
on one origin — `/api/*` goes to the backend with the prefix stripped, everything else to the frontend:

```caddy
your-domain.com {
    handle_path /api/* {
        reverse_proxy 127.0.0.1:8080
    }

    handle {
        reverse_proxy 127.0.0.1:3000
    }
}
```

Because the browser only ever sees one origin, the backend needs no CORS entry for production, and no API URL is
baked into the build — the same image works in every environment.

## Suggested backend integration later

- `POST /auth/login`
- `POST /registration/student`
- `POST /registration/instructor`
- `GET /admin/student-registrations`
- `GET /admin/instructor-registrations`
- `POST /admin/student-registrations/{id}/approve`
- `POST /admin/student-registrations/{id}/reject`
- `POST /admin/instructor-registrations/{id}/approve`
- `POST /admin/instructor-registrations/{id}/reject`

Keep JWT handling in a small auth client/service first, then add middleware once API contracts are stable.
