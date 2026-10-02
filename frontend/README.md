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
cp .env.example .env   # set VITE_API_BASE_URL if the API isn't on localhost:8080
npm install
npm run dev            # http://localhost:3000
npm run build          # type-check + static build into dist/
npm run preview        # serve dist/ locally
npm test               # run tests in watch mode (npm run test:run for a single run)
```

The dev server is pinned to port 3000 because the backend CORS config only allows that origin.

## Deploying

`npm run build` outputs static files in `dist/`. Because routing is client-side, the web server must fall back to
`index.html` for unknown paths, otherwise refreshing a deep link like `/dashboard/admin` returns 404. For nginx:

```nginx
location / {
    try_files $uri /index.html;
}
```

`VITE_API_BASE_URL` is inlined at build time, so set it before running `npm run build`, not at container start.

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
