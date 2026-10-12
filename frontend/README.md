# University Enrollment Frontend

Single-page dashboard UI (Vite + React Router) for the Spring Boot University Enrollment Management project.

## Screens

**Everyone**: login, student and instructor registration requests, course catalogue (search and department filter), profile.

**Admin**
- Dashboard with pending requests, semesters open for registration and offerings scheduled
- Student and instructor registrations: status tabs, department filter, approve/reject with confirmation
- Semesters: list, create, detail with registration window, actions and the semester's offerings
- Course offerings: list with semester/department/instructor filters, create (department narrows course and instructor)
- Offering detail with its class list: approve or reject enrollment requests
- Students and instructors by department; create course

**Instructor**
- Dashboard with offerings taught, students enrolled and offerings open for registration
- My offerings grouped by semester, each opening its class list to approve or reject requests

**Student**
- Dashboard with a banner while registration is open, plus enrolled and pending counts
- Enroll in courses: offerings open for registration (or all semesters), request a seat
- Enrollments: every request with cancel and drop
- My courses grouped by semester with credit totals

Actions shown in the UI come from the server's `allowedActions`, so the UI never offers something the API would refuse.
List filters are kept in the URL, so a filtered view survives refresh and can be shared.

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

## Known gaps (need server changes)

- **Re-requesting an offering:** `ENROLL` ignores an existing request, so a card can show PENDING or REJECTED and still
  offer "Request enrollment"; the server then answers `409`. Waiting on the decision about re-requests after a
  cancelled, rejected or dropped enrollment.
- **Where a course is offered:** `GET /course-offerings` has no `courseId` filter, so the catalogue can't list a
  course's offerings.
- **Pending requests per offering:** instructors can't see "3 requests waiting" on the dashboard without one roster
  call per offering; a `pendingCount` on the offering response would cover it.
- **No pagination:** every list loads in full, which is fine at demo scale.
