# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Spring Boot 4.x (Java 21), Spring Security (JWT, stateless), Spring Data JPA, MySQL, Flyway, Lombok, JUnit Jupiter 6.

## Commands

```bash
# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=StudentRegistrationServiceIntegrationTests

# Build (skip tests)
./mvnw package -DskipTests

# Test coverage report (generated at target/site/jacoco/index.html)
./mvnw test jacoco:report
```

## Configuration

`application.yml` contains the shared base config, including `server.forward-headers-strategy: framework` — in production the API runs behind Caddy over plain HTTP, and Spring must honour `X-Forwarded-Proto/Host` so same-origin browser requests aren't rejected as cross-origin by CORS (covered by `ReverseProxyCorsTests`).

The `dev` profile (`application-dev.yml`) is committed and works out of the box against the dev MySQL from the repo root's `compose.dev.yml` (`localhost:3306/universityManagementDev`, `university/university`); every value is an env placeholder with a dev default (`DB_URL`, `JWT_SECRET`, `ADMIN_USERNAME`, ...), and the dev admin is `admin/admin123`. Never put real secrets in it. The `prod` profile (`application-prod.yml`) reads everything from environment variables, supplied by `deploy/compose.yml` from the VPS `.env`. Activate a profile with `-Dspring-boot.run.profiles=...` or `SPRING_PROFILES_ACTIVE`.

`info.app.version` is `@project.version@`, filled in from `pom.xml` at build time and reported by `/actuator/info` (verified by `AppInfoTests`). The `pom.xml` version must equal the newest `api` entry in the repo root's `versions.yml` — CI enforces it; bump both together when releasing.

Required properties (all overridden in dev profile):
- `spring.datasource.url/username/password`
- `app.jwt.secret` — must be ≥256 bits
- `app.admin.username/password/email` — seeds the admin user on startup via `AdminSeeder`

## Architecture

### Domain model

- **People:** `UserEntity` (holds the `Role`: `ADMIN`, `INSTRUCTOR`, `STUDENT`), with `StudentEntity` / `InstructorEntity` linked to a user. Both belong to a `DepartmentEntity`.
- **Registration workflow:** `StudentRegistrationEntity`, `InstructorRegistrationEntity`.
- **Academics:** `CourseEntity` (belongs to a department) → `CourseOfferingEntity` (a course taught by an instructor in a `SemesterEntity`, with a `section`, `capacity` and `enrolled` count) → `EnrollmentEntity` (a student in an offering).

### API conventions

Every endpoint follows these rules; keep new ones consistent with them.

1. **Plural nouns, no role in the URL.** There is no `/admin/...`; `@PreAuthorize` decides who may call what.
2. **The caller's own data lives under `/me`** (`/me`, `/me/enrollments`, `/me/course-offerings`). Every other collection returns the same data whoever calls it. Each module owns its own `/me/...` controller (`MyEnrollmentsController`, `MyCourseOfferingsController`), so `user/` never depends on other modules.
3. **Filters are query parameters with shared names:** `status`, `semesterId`, `departmentId`, `instructorId`. They bind to a `*Filter` record in the module's `dto/`, taken as an unannotated controller parameter (never required; a missing param is `null`, and `null` means "don't filter"). The repo has a `default search(XFilter)` that unpacks the record into an explicit `@Query` with `(:param IS NULL OR ...)` clauses. Adding a filter later is non-breaking, so only add one when a screen needs it.
4. **Scope is never a filter.** Anything the server decides (the current student or instructor) is a separate argument taken from the principal, never a field of the bound record, or `?studentId=` would let a client read someone else's data. See `EnrollmentRepo.searchForStudent(studentId, filter)` and `CourseOfferingRepo.searchForInstructor(instructorId, filter)`.
5. **State changes are `POST /{resource}/{id}/{action}`** with an action enum and a path converter, and the response carries `allowedActions` (see below).
6. **Nested objects are `*Summary` records** (`CourseSummary`, `InstructorSummary`, `StudentSummary`, `SemesterSummary`, `CourseOfferingSummary`; `DepartmentResponse` is small enough to serve as the department summary). Only the top-level object is a full `*Response`.
7. **Timestamps are `Instant`** (`createdAt`, `updatedAt`, `reviewedAt`); the client formats them in its own timezone. Calendar dates such as `dateOfBirth` stay `LocalDate`.
8. **Lists are flat and sorted deterministically**, ending in `id` as a tiebreaker. Grouping for display is the client's job. Within a year, `SUMMER` comes before `WINTER` everywhere (semesters, offerings, enrollments). The term is sorted as its stored string, which happens to match; adding a term would need an explicit order (`CASE` or an ordinal column).

### Endpoints

| Endpoint | Who may call it |
|---|---|
| `POST /auth/login` | public |
| `GET /me` (`MeResponse`, also returned by login) | any logged-in user |
| `GET /me/enrollments?semesterId&status` | student (own rows only) |
| `GET /me/course-offerings?semesterId&departmentId` | instructor (own offerings only) |
| `POST /student-registrations`, `POST /instructor-registrations` | public, **POST only** |
| `GET /student-registrations?status&departmentId` (and instructor) | admin; no default status, oldest request first |
| `POST /student-registrations/{id}/{action}` (and instructor) | admin |
| `GET /departments` | public |
| `GET /courses?departmentId`, `GET /courses/{id}` | public |
| `POST /courses` | admin |
| `GET /students?departmentId`, `GET /instructors?departmentId` | admin |
| `GET /semesters`, `GET /semesters/{id}` | logged in |
| `POST /semesters`, `POST /semesters/{id}/{action}` | admin |
| `GET /course-offerings?semesterId&departmentId&instructorId`, `GET /course-offerings/{id}` | logged in, same result for every role |
| `POST /course-offerings` | admin |
| `GET /course-offerings/{id}/enrollments?status` (the roster) | admin or the offering's instructor |
| `POST /course-offerings/{id}/enrollments` | student, for themselves |
| `GET /enrollments/{id}`, `POST /enrollments/{id}/{action}` | `@CanAccessEnrollment` |

`MeResponse` is `(id, username, email, role, profile)`; `profile` is `null` for an admin, otherwise the student or instructor row (`profile.id` is the student or instructor id, not the user id). Login builds it from the principal returned by `authenticate(...)`, because the security context is still empty during login; a student or instructor without a profile row gets a 404 at login.

### Registration workflow

New users go through a two-step registration process:
1. A public `POST /student-registrations` or `POST /instructor-registrations` creates a registration with status `PENDING`. Its `allowedActions` is always empty, because the anonymous registrant has no role to ask the policy about.
2. An admin reviews it with `POST /{student|instructor}-registrations/{id}/approve|reject`. Approval creates the `UserEntity` and the matching `StudentEntity` / `InstructorEntity`, and records `reviewedBy` and `reviewedAt`.

`StudentRegistrationService` and `InstructorRegistrationService` own this logic and are kept identical apart from the profile they create; change them together.

**Who may request which username and email** (`RegistrationValidator.validateCredentialsAvailable`): a value is taken when a user account or a `PENDING` registration of either kind holds it. A rejected request frees it, so the person can apply again. Every case answers with the same `"Username or email is not available"`, so the public endpoint doesn't reveal which accounts or requests exist. The registration tables therefore have no unique constraints on these columns (dropped in `V6`); the `users` table constraints remain the backstop, so two racing requests for the same name can both be pending, but only one can ever be approved.

### Status workflows (semester, enrollment, registration)

Semesters, enrollments and registrations are state machines, and all three follow the same structure. Copy it for any new workflow.

| Piece | Semester | Enrollment | Registration |
|---|---|---|---|
| Status, owns the transition table in `canTransitionTo` (exhaustive arrow `switch`) | `SemesterStatus`: `PLANNED → ACTIVE → COMPLETED`, `PLANNED/ACTIVE → CANCELLED` | `EnrollmentStatus`: `PENDING → ENROLLED/REJECTED/CANCELLED`, `ENROLLED → DROPPED` | `RegistrationStatus`: `PENDING → APPROVED/REJECTED` |
| Action, maps to a target status | `SemesterAction`: `ACTIVATE`, `COMPLETE`, `CANCEL` | `EnrollmentAction`: `APPROVE`, `REJECT`, `CANCEL`, `DROP` | `RegistrationAction`: `APPROVE`, `REJECT` |
| Policy, decides who may do what | `SemesterActionPolicy` (admin only) | `EnrollmentActionPolicy` (see below) | `RegistrationActionPolicy` (admin only) |
| Endpoint | `POST /semesters/{id}/{action}` | `POST /enrollments/{id}/{action}` | `POST /{student\|instructor}-registrations/{id}/{action}` |
| Path converter (case-insensitive, trims) | `StringToSemesterActionConverter` | `StringToEnrollmentActionConverter` | `StringToRegistrationActionConverter` |
| Invalid transition | 400 | 400 | 409 |

Status filters (`?status=`) bind through `StringToEnrollmentStatusConverter` and `StringToRegistrationStatusConverter`, which are case-insensitive in the same way.

Rules that hold across all three:

- **Responses carry `allowedActions`** for the current viewer, derived from the same policy method that enforces the action (`canPerform` / `validate`). Enforcement and what the UI shows therefore cannot disagree. The frontend renders these and never duplicates transition rules.
- **`EnrollmentActionPolicy` separates who from what.** `getActor` maps the viewer to `ADMIN`, `OFFERING_INSTRUCTOR`, `OWNING_STUDENT` or `NONE`; `PERMITTED_ACTIONS` lists what each actor may do; `businessRule` then checks the transition, a closed semester and a full offering, in that order. A closed (`COMPLETED` or `CANCELLED`) semester blocks `APPROVE`, `CANCEL` and `DROP`, so the student's side is frozen; `REJECT` is still allowed, so reviewers can clear leftover requests. `validate` returns an `EnrollmentDenial`, which `toException` maps to 403 / 400 / 400 / 409.
- **Actions lock rows.** Transitions load with `findForUpdateById` (`PESSIMISTIC_WRITE`). Enrollment actions lock the course offering *before* validating, because a locking query does not refresh an entity already in the persistence context. Loading it unlocked first would validate against stale seat counts. Registration actions lock the registration, so two admins reviewing at once cannot both create the user. `EnrollmentConcurrencyTests`, `SemesterConcurrencyTests` and `*RegistrationConcurrencyTests` cover this.
- **Locking queries have no `@EntityGraph`.** `SELECT ... JOIN ... FOR UPDATE` in MySQL also locks every joined row (semester, course, department), which would serialize unrelated actions. Load the associations lazily inside the transaction instead.

The course offering has one action without an endpoint of its own: `ENROLL`, exposed through `CourseOfferingResponse.allowedActions` and performed by `POST /course-offerings/{id}/enrollments`. `CourseOfferingActionPolicy.validateEnroll` checks the role, then the registration window, then free seats, and returns an `EnrollDenial`. Two deliberate decisions:

- **`ENROLL` reflects the offering's rules only**, not whether this student has already requested it (the one exception to "`allowedActions` equals enforcement"). The duplicate is rejected at creation with 409 `ALREADY_REQUESTED`, after the policy, so a retry on a closed or full offering reports the rule that applies to everyone.
- **Requests for a full offering are blocked** with 409 `OFFERING_FULL`; there is no waitlist. Creating a request takes no lock, because a request doesn't take a seat; `APPROVE` does, under the offering lock.

Open decisions:
- Re-requesting after `CANCELLED`, `REJECTED` or `DROPPED` is blocked by the `unique_enrollment` constraint, and the student gets `ALREADY_REQUESTED`. Allowing it would mean reusing the existing row.
- Cancelling a semester leaves its enrollments as they are. They should be cancelled or rejected with it; planned for a later PR.

### Security

Stateless JWT auth. `JwtAuthenticationFilter` validates tokens on every request; an expired or invalid token, or one for a deleted user, gets 401. The principal is rebuilt from the database on every request (`new UserPrincipal(user)`, carrying `id`, `username`, `email`, `role`), so a role change applies immediately. Everything not listed in `PublicEndpointConfig` requires authentication: login, `POST` on both registration paths, `GET /departments`, `GET /courses` and `/courses/{id}`, and health/info.

**Public matchers must name the HTTP method** when the path also serves a protected method. `/student-registrations` is a public `POST` but an admin-only `GET`; a path-only matcher would make the admin list public at the route level and leave only `@PreAuthorize` in the way.

Method-level rules use `@PreAuthorize`, either with role expressions from `AuthorizationExpressions` (`ADMIN`, `ANY_AUTHENTICATED`, ...) or with meta-annotations in `security/annotation` that call `AuthorizationService`:

- `@AdminOrCourseOfferingInstructor`: admin, or the instructor of `#courseOfferingId`
- `@CanAccessEnrollment`: admin, the offering's instructor, or the enrolled student
- `@CurrentStudent`: `#studentId` is the logged-in student (now only on `createEnrollment`)

The annotations answer "is this user involved with this resource at all?" using repo `exists` queries. The action policies answer "may they perform this specific action?". The relationship facts appear in both places on purpose, and `EnrollmentAuthorizationTest.AnnotationPolicyConsistency` keeps the two in agreement.

**`@PreAuthorize` goes on use cases, not building blocks.** Methods that controllers call and that return a `*Response` are guarded. Methods that return an `*Entity` and are called by other services (`getStudentEntity`, `getInstructorByUserId`, `create*ForUser`, ...) are not: the calling use case already holds the precise rule, a role check there could only be rougher, and login calls some of them before any security context exists. A controller never returns an entity, so an unguarded entity getter can't be reached over HTTP. If a building block is ever needed by a controller, add a guarded `*Response` method that calls it.

**`/me` lists take their scope from the principal** under a plain role check (`@PreAuthorize(STUDENT)` / `INSTRUCTOR`), not from a `#studentId` parameter, so there is nothing for the client to forge.

`CurrentUserService` reads the current `UserPrincipal` from the security context (id and role need no extra query) and throws `AuthenticationCredentialsNotFoundException` when there is none, including for anonymous requests on public routes. `getCurrentStudent()` / `getCurrentInstructor()` check the role from the principal (403 for the wrong role) and then load the profile row.

### Error handling

Throw the domain exceptions from `common/exceptions`: `ResourceNotFoundException` (404), `BadRequestException` (400), `ForbiddenException` (403), `ConflictException` (409). `GlobalExceptionHandler` turns them, and Spring's own exceptions (validation, type mismatch, unreadable body, unknown route, wrong method, auth failures), into `ApiErrorResponse` with a message the UI shows as-is. So write messages for end users, and never let Java type or package names through: a bad path variable and a bad filter value (which arrives as a binding failure inside `MethodArgumentNotValidException`, not as `MethodArgumentTypeMismatchException`) both become `"Invalid value 'foo' for 'status'. Allowed values: ..."`. Client errors are logged at `WARN` without a stack trace; unexpected errors at `ERROR` with one.

### API response pattern

All controllers return `ResponseEntity<ApiResponse<T>>` (success) or `ResponseEntity<ApiErrorResponse<T>>` (error) via the `Res` factory class. Use `Res.success(SuccessCode.CREATED, body)` / `Res.error(ErrorCode.X, message)` rather than constructing responses manually. Return flat lists of resources (for example, `GET /courses` returns `List<CourseResponse>`, each with its department); grouping for display is the client's job.

### Mappers

Each domain package has a `*Mapper` class with static methods for converting between entities, DTOs, and internal command objects. No MapStruct; all mappings are hand-written.

### Time

Inject the `Clock` bean (`TimeConfig`) and use `LocalDate.now(clock)` / `Instant.now(clock)`; never call `now()` without it. JPA auditing (`createdAt` / `updatedAt`) follows the same clock through the `auditingDateTimeProvider` in `JpaConfig`. Tests replace it with `MutableTestClock` (`TestClockConfig`), fixed at `2026-01-10T00:00:00Z`, which keeps registration windows and timestamps deterministic. A test that needs different times calls `setTo(instant)`; `IntegrationTests` and `RepoTests` reset it after every test, and concurrency tests must reset it themselves if they change it.

### Package layout

Packages are by domain. Each holds its own controller / service / repo / entity / mapper, with `dto/` (requests, responses, summaries and `*Filter` records), `types/` (enums only) and, where needed, `converter/` and `validators/` sub-packages.

```
auth/            — login, JWT issuance, AuthorizationExpressions constants
admin/           — AdminSeeder, AdminUser
registration/    — RegistrationStatus/Action/Policy and converters; student/ and instructor/ each with controller, service, repo
user/            — UserEntity, UserService, CurrentUserService, MeController (GET /me)
student/         — StudentEntity, StudentService, GET /students
instructor/      — InstructorEntity, InstructorService, InstructorValidator, GET /instructors
department/      — DepartmentEntity, public GET /departments
course/          — CourseEntity, GET/POST /courses
semester/        — SemesterEntity, SemesterStatus/Action, SemesterActionPolicy, SemesterValidator
courseOffering/  — CourseOfferingEntity (capacity / enrolled seats), CourseOfferingActionPolicy (ENROLL), offerings and their roster, /me/course-offerings
enrollment/      — EnrollmentEntity, EnrollmentStatus/Action/Denial, EnrollmentActionPolicy, /me/enrollments
security/        — SecurityConfig, jwt/, UserPrincipal, AuthorizationService, annotation/
config/          — JpaConfig, AppSecurityBeansConfig, PublicEndpointConfig, TimeConfig
common/          — rest (Res, ApiResponse, ErrorCode, SuccessCode), exceptions, entity (BaseEntity), types
```

### Database

Flyway owns the schema in every profile. Migrations live in `src/main/resources/db/migration/` (`V1__initial_schema.sql` … `V6__allow_re_registration_after_rejection.sql`), and `ddl-auto=validate` makes Hibernate check the entity mappings against them. Schema changes always go in a new `V{n}__description.sql`; never edit an applied migration.

JPA uses `PhysicalNamingStrategyStandardImpl`, so column and table names match exactly what you write in the entity (no automatic camelCase → snake_case conversion).

Invariants are enforced twice: in code with a clear message, and in the database as a backstop. Examples are unique semester `term + year`, unique course `code`, unique offering `course + semester + section`, the `chk_*` date, credits and seat checks, and `0 <= enrolled <= capacity`.

## Testing

Tests run against MySQL in Testcontainers (`MySqlTestContainer`), so Docker must be running.

- **Base classes** (`config/`): `IntegrationTests` (`@SpringBootTest`, rolled back per test), `RepoTests` (`@DataJpaTest`) and `WebIntegrationTests` (`IntegrationTests` plus `@AutoConfigureMockMvc`; `bearer(user)` / `bearer(role)` mint real JWTs, so requests go through `JwtAuthenticationFilter`). Concurrency tests extend `MySqlTestContainer` directly without `@Transactional`, because threads must see each other's commits; they delete exactly what they created in `@AfterEach`.
- **Test data** (`testUtils/`): `fixtures/` (pre-filled entity and request builders), `seeders/` (save fixtures, with counters for unique values), `scenario/` (builders that seed a whole graph, e.g. `EnrollmentScenarioSeeder`). Use these; don't build ad-hoc helpers.
- **Authentication:** `RoleActor` (`ADMIN`, `INSTRUCTOR`, `STUDENT`, `ANONYMOUS`, each with `authenticate()`) and `AuthOutcome` (with `expectedException()`) for role-only authorization tables; `TestAuthentication.asAdmin()`, `asRole(Role)`, `asUser(UserEntity)`, `asStudent(...)`, `asInstructor(...)` and `clear()`, or the `@WithAdmin` / `@WithInstructor` / `@WithStudent` annotations. Clear the context in `@AfterEach` when setting it by hand.
- **Style:** prefer `@ParameterizedTest` tables. Transition tests list the valid cases and derive the invalid ones as "all pairs minus valid". Authorization tests are actor × outcome tables (`ALLOWED`, `ACCESS_DENIED`, `UNAUTHENTICATED`) that start from a state where the action is otherwise valid, so a denial can only come from authorization.
- **Service tests vs MockMvc tests:** business rules, authorization tables and filters are tested at the service level. MockMvc tests cover only what needs HTTP: route security (`RouteSecurityWebTests`: public vs 401 per route, token handling, 403 envelope), binding (`RequestBindingWebTests`: case-insensitive path actions and status filters, readable 400s) and the response envelope (`ResponseShapeWebTests`). Add a row there when you add a route.
- **Scoped queries** need a test where another owner's rows match the filter, so only the scope can keep them out (`shouldNeverReturnAnotherStudentsEnrollments`).
- **After renaming or moving classes, run `./mvnw clean test`.** A stale class left in `target/` can register a second bean for the same route and fail the whole context.
