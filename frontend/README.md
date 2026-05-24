# University Enrollment Frontend

Starter Next.js dashboard UI for the Spring Boot University Enrollment Management project.

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
npm run dev
```

Open `http://localhost:3000`.

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
