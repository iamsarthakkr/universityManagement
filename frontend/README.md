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
proxy `/api/*` to the backend at `http://localhost:8080`, stripping the `/api` prefix, so start the backend first
(see "Running Locally" in the root README: dev MySQL via `compose.dev.yml`, then the backend with the `dev`
profile from your IDE or `./mvnw`).

Use Node 22 (`.nvmrc`). The frontend's Docker image is only used in production (`deploy/compose.yml`).

## Deploying

The production image (`Dockerfile` + `nginx.conf`) and how it's deployed are described in the root
[DEPLOY.md](../DEPLOY.md).

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
