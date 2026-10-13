# University Enrollment Management System

A backend-focused university enrollment platform built with **Spring Boot, JPA, MySQL, and Spring Security**.

The project goes beyond basic CRUD by modelling real enrollment workflows including semester registration windows, course offerings, role/resource-based authorization, enrollment state transitions, capacity limits, transactional consistency, and concurrent enrollment handling.

## Features

- Student and instructor registration, reviewed by an admin
- Department management
- Public course catalogue
- Semester lifecycle management
- Registration windows
- Course offerings with instructors and capacity
- Enrollment request, approval, cancellation, and drop workflows
- Every response tells the UI which actions the viewer may take next (`allowedActions`)
- A `/me` namespace for the logged-in user's own profile, enrollments and teaching load
- Role-based and resource-level authorization
- Database migrations with Flyway
- Integration, repository and HTTP-level (MockMvc) testing
- Concurrency-safe enrollment approval and registration review
- Database-level duplicate enrollment protection
- React dashboard for admins, instructors and students that renders only the actions the server allows (see `frontend/README.md`)

## Domain Model

```text
Department
├── Students
├── Instructors
└── Courses

Course
└── CourseOffering
    ├── Semester
    ├── Instructor
    └── Enrollments
        └── Student
```

### Core Models

- **User** — authentication identity and application role
- **Student** — student academic identity and department
- **Instructor** — instructor identity and department
- **Department** — academic department
- **Course** — reusable academic course definition
- **Semester** — academic period and registration window
- **CourseOffering** — a course scheduled for a specific semester, instructor, and capacity
- **Enrollment** — student registration for a specific course offering

A student enrolls in a **CourseOffering**, not directly in a Course. This preserves semester, instructor, capacity, and enrollment-history context.

## Architecture

The backend follows a layered, domain-oriented structure:

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
 ├── Business Rules
 ├── Authorization
 └── Validators
     │
     ▼
Repository
     │
     ▼
JPA / Hibernate
     │
     ▼
MySQL
```

REST APIs use request/response DTOs rather than exposing JPA entities directly.

Business workflows and transactional boundaries live primarily in the service layer.

## API

The API follows a few rules everywhere:

- **Plural resources, no role in the URL.** There is no `/admin/...`; method-level authorization decides who may call what.
- **The caller's own data lives under `/me`.** Every other collection returns the same data whoever asks.
- **Filters are optional query parameters with shared names** (`status`, `semesterId`, `departmentId`, `instructorId`). The data a caller is limited to (for example "my enrollments") always comes from the logged-in user, never from a parameter.
- **State changes are `POST /{resource}/{id}/{action}`**, for example `POST /enrollments/12/approve`.
- **Nested objects are compact summaries**, and timestamps are UTC instants.
- **Every response has the same envelope:** `{ message, body, isSuccess, timestamp }` on success and `{ message, errors, isSuccess, timestamp }` on error. Error messages are written for end users and never contain internal type names.

| Area | Endpoints |
| --- | --- |
| Auth | `POST /auth/login` (public), `GET /me` |
| Registration | `POST /student-registrations`, `POST /instructor-registrations` (public); `GET` and `POST /{id}/{approve\|reject}` on both (admin) |
| People | `GET /students`, `GET /instructors` (admin) |
| Catalogue | `GET /departments`, `GET /courses`, `GET /courses/{id}` (public); `POST /courses` (admin) |
| Semesters | `GET /semesters`, `GET /semesters/{id}`; `POST /semesters`, `POST /semesters/{id}/{activate\|complete\|cancel}` (admin) |
| Offerings | `GET /course-offerings`, `GET /course-offerings/{id}`, `GET /me/course-offerings` (instructor); `POST /course-offerings` (admin) |
| Enrollments | `POST /course-offerings/{id}/enrollments` (student), `GET /course-offerings/{id}/enrollments` (roster), `GET /me/enrollments` (student), `GET /enrollments/{id}`, `POST /enrollments/{id}/{approve\|reject\|cancel\|drop}` |

## Registration Workflow

New users apply through a public registration request, which an admin approves or rejects:

```text
PENDING ──► APPROVED   (creates the user and the student/instructor profile)
   └──────► REJECTED
```

A username or email is unavailable while a user account or a pending request holds it. A rejected request frees it, so the person can apply again. The public endpoint answers every unavailable case with the same message, so it doesn't reveal which accounts exist.

## Enrollment Workflow

Enrollment is treated as a domain workflow rather than a simple many-to-many relationship.

Lifecycle:

```text
PENDING
   ├── approve (admin / offering instructor) ──► ENROLLED ── drop (student) ──► DROPPED
   ├── reject  (admin / offering instructor) ──► REJECTED
   └── cancel  (student) ──────────────────────► CANCELLED
```

Before an enrollment is requested, the system checks that the caller is a student, the semester's registration window is open, the offering has a free seat, and the student hasn't already requested it.

Before an action is applied, it checks who is acting, that the transition is valid, that the semester is still open (approve, cancel and drop are frozen once it is completed or cancelled; reject is not), and, for approval, that a seat is free.

Each semester, offering, enrollment and registration response carries `allowedActions` for the current viewer, computed by the same policy that enforces the action. The UI shows exactly those buttons, so what it offers and what the server allows cannot disagree.

## Concurrency & Data Consistency

Enrollment approval is capacity-sensitive.

For example, if an offering has one seat remaining, two concurrent approval requests must not both observe the seat as available.

The application protects these workflows using:

- Spring transactions
- database locking for capacity-sensitive operations
- locking of enrollment state where concurrent transitions matter
- database constraints for invariant enforcement

Duplicate enrollment is additionally protected by a database uniqueness constraint equivalent to:

```sql
UNIQUE(student_id, course_offering_id)
```

This provides a final consistency guarantee even if concurrent requests pass application-level validation simultaneously.

## Authorization

Authorization combines **role-based access** with **resource ownership**. Public routes (login, registration requests, the course catalogue, departments) are matched by HTTP method as well as path, so a public `POST /student-registrations` doesn't open the admin-only `GET` on the same path.

Examples:

```text
ADMIN
└── access administrative resources

INSTRUCTOR
└── access enrollment/offering resources they teach

STUDENT
└── access enrollment resources belonging to them
```

Spring Security method-level authorization and reusable custom authorization rules are used to avoid relying only on controller-level role checks. Rules sit on the operations a client can call, not on the internal helpers those operations share, so each check states the precise rule for its use case.

## Key Design Decisions

### Course vs Course Offering

`Course` represents the reusable academic definition.

`CourseOffering` represents that course being taught during a specific semester with its own:

- instructor
- capacity
- status
- enrollments

### Enrollment as a Domain Entity

Enrollment has its own lifecycle and business rules, so it is modelled as a first-class entity instead of a simple join table.

### Capacity Checked During Approval

Pending enrollments do not reserve seats, but a request for a full offering is refused up front; there is no waitlist.

Capacity is enforced again, under a row lock, when the enrollment is approved.

### Database Constraints as Final Guarantees

Application validation provides meaningful domain errors, while database constraints protect invariants under concurrent execution.

### Deterministic Time

Business rules, audit timestamps and review times all read one injectable clock, so tests pin and move time instead of depending on when they run.

### Flyway for Schema Management

Database schema changes are versioned through Flyway migrations instead of relying on automatic production schema generation by Hibernate.

### Explicit Fetching

JPA relationships are generally lazy.

Where related data is required, explicit fetching strategies such as `JOIN FETCH`, entity graphs, or DTO queries are preferred to avoid unnecessary object graphs and N+1 queries.

Queries that lock rows for an update don't fetch related tables, because MySQL would lock every joined row too.

## Tech Stack

| Area        | Technology                  |
| ----------- | --------------------------- |
| Language    | Java 21                     |
| Framework   | Spring Boot                 |
| REST        | Spring Web                  |
| Persistence | Spring Data JPA / Hibernate |
| Security    | Spring Security             |
| Database    | MySQL                       |
| Migrations  | Flyway                      |
| Validation  | Jakarta Bean Validation     |
| Testing     | JUnit 5, Spring Boot Test   |
| Build       | Maven                       |

## Testing

The project primarily uses integration and persistence-layer tests for workflows involving:

- JPA relationships
- transactions
- database constraints
- authorization
- status transitions
- registration windows
- enrollment capacity

Reusable test scenario seeders are used to create related domain data such as students, courses, semesters, and course offerings.

Concurrency-sensitive operations (enrollment approval, semester transitions, registration review) also have dedicated test coverage.

HTTP-level tests (MockMvc with real JWTs) cover what service tests can't: which routes are public, how tokens are handled, request binding, and the response envelope.

The frontend is tested with Vitest and Testing Library against a mocked `fetch`, so the HTTP client, React Query and the real router stay in the path under test.

## Project Structure

```text
universityManagement/
├── .github/workflows/            # ci.yml (tests) and release.yml (versioned images)
├── deploy/                        # production compose + deploy/rollback scripts (see DEPLOY.md)
├── compose.dev.yml                # local MySQL for development
├── versions.yml                   # release history of the api and frontend (see DEPLOY.md)
├── frontend/                      # Vite + React SPA
└── server/
    ├── src/main/java/
    ├── src/main/resources/
    │   └── db/migration/
    ├── src/test/
    ├── pom.xml
    └── mvnw
```

The backend is organized primarily by domain, including areas such as:

```text
auth/
user/
department/
student/
instructor/
course/
semester/
courseOffering/
enrollment/
registration/
security/
common/
```

## Running Locally

### Requirements

- Java 21
- Node.js 22 (see `frontend/.nvmrc`)
- Docker (local MySQL and the backend's Testcontainers tests)

### Start the stack

```bash
git clone https://github.com/iamsarthakkr/universityManagement.git
cd universityManagement

docker compose -f compose.dev.yml up -d --wait     # MySQL on localhost:3306

cd server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev     # API on localhost:8080

cd ../frontend
npm install
npm run dev                                                # app on localhost:3000
```

The `dev` profile (`server/src/main/resources/application-dev.yml`) is committed and works out of the box against the
dev MySQL container; every value can be overridden with environment variables (`DB_URL`, `JWT_SECRET`, ...). Flyway
creates the schema on first start, and a dev admin is created with `admin` / `admin123`.

On an empty database the `dev` profile also seeds demo data from
`server/src/main/resources/seed/seed.example.json` (generated by `server/seed-tools/generate_seed.py`): two
departments with their instructors, students and courses, two completed semesters with their enrollment history, a
running one with enrolled and pending requests, a planned one, and approved, pending and rejected registrations.
Every seeded account's password is `password` (for example `riya.sharma` as a student or `ada.lovelace` as an
instructor). Set `SEED_MODE=` (blank) to skip it, or
`SEED_MODE=rollback` to remove it; see [Demo data](DEPLOY.md#demo-data). A dev database that already holds a
department or user with the same code or username makes startup fail with the conflict; recreate the dev database
(`docker compose -f compose.dev.yml down -v`) or set `SEED_MODE=`.

The frontend calls the API under `/api` on its own origin; the Vite dev server proxies `/api/*` to `localhost:8080`.

Only MySQL runs in Docker during development. The application images are built and checked by CI on every pull
request.

### Tests

```bash
cd server && ./mvnw verify          # backend (Testcontainers, needs Docker)
cd frontend && npm run test:coverage  # frontend (Vitest)
```

## Database Migrations

Flyway migration files are stored under:

```text
server/src/main/resources/db/migration/
```

Schema evolution is version-controlled and applied automatically during application startup.

## Versions

The API and frontend are versioned independently in `versions.yml`, kept in sync with `server/pom.xml` and
`frontend/package.json`. The running versions are reported by `GET /actuator/info` and shown in the user menu.
Releasing a new version is described in [DEPLOY.md](DEPLOY.md#releasing-a-version).

## Deployment

Production deployment (VPS, Caddy, shared MySQL, CI/CD, rollback) is documented in [DEPLOY.md](DEPLOY.md).

## Repository

https://github.com/iamsarthakkr/universityManagement
