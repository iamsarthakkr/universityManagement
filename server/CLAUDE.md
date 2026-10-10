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

### Registration workflow

New users go through a two-step registration process:
1. Public POST to `/registration/student` or `/registration/instructor` creates a registration with status `PENDING`.
2. An admin approves or rejects it via `POST /admin/student-registrations/{id}/approve|reject` (and `/admin/instructor-registrations/...`). Approval creates the `UserEntity` and the matching `StudentEntity` / `InstructorEntity`.

`StudentRegistrationService` and `InstructorRegistrationService` own this logic. `@PreAuthorize(AuthorizationExpressions.ADMIN)` guards all admin actions.

### Status workflows (semester and enrollment)

Semesters and enrollments are state machines, and both follow the same structure. Copy it for any new workflow.

| Piece | Semester | Enrollment |
|---|---|---|
| Status, owns the transition table in `canTransitionTo` (exhaustive arrow `switch`) | `SemesterStatus`: `PLANNED → ACTIVE → COMPLETED`, `PLANNED/ACTIVE → CANCELLED` | `EnrollmentStatus`: `PENDING → ENROLLED/REJECTED/CANCELLED`, `ENROLLED → DROPPED` |
| Action, maps to a target status | `SemesterAction`: `ACTIVATE`, `COMPLETE`, `CANCEL` | `EnrollmentAction`: `APPROVE`, `REJECT`, `CANCEL`, `DROP` |
| Policy, decides who may do what | `SemesterActionPolicy` (admin only) | `EnrollmentActionPolicy` (see below) |
| Endpoint | `POST /semesters/{id}/{action}` | `POST /enrollments/{id}/{action}` |
| Path converter (case-insensitive, trims) | `StringToSemesterActionConverter` | `StringToEnrollmentActionConverter` |

Rules that hold across both:

- **Responses carry `allowedActions`** for the current viewer, computed by the same policy method that enforces the action. Enforcement and what the UI shows therefore cannot disagree. The frontend renders these and never duplicates transition rules.
- **`EnrollmentActionPolicy` separates who from what.** `getActor` maps the viewer to `ADMIN`, `OFFERING_INSTRUCTOR`, `OWNING_STUDENT` or `NONE`; `PERMITTED_ACTIONS` lists what each actor may do; `businessRule` then checks the transition, a closed semester and a full offering, in that order. `validate` returns an `EnrollmentDenial`, which `toException` maps to 403 / 400 / 400 / 409.
- **Actions lock rows.** Transitions load with `findForUpdateById` (`PESSIMISTIC_WRITE`). Enrollment actions lock the course offering *before* validating, because a locking query does not refresh an entity already in the persistence context. Loading it unlocked first would validate against stale seat counts. `EnrollmentConcurrencyTests` and `SemesterConcurrencyTests` cover this.

### Security

Stateless JWT auth. `JwtAuthenticationFilter` validates tokens on every request; an expired or invalid token, or one for a deleted user, gets 401. `SecurityConfig` defines route-level rules; everything not listed in `PublicEndpointConfig` (a `RequestMatcher` bean: login, registration, `GET /departments`, health/info) requires authentication.

Method-level rules use `@PreAuthorize`, either with role expressions from `AuthorizationExpressions` (`ADMIN`, `ANY_AUTHENTICATED`, ...) or with meta-annotations in `security/annotation` that call `AuthorizationService`:

- `@AdminOrCourseOfferingInstructor`: admin, or the instructor of `#courseOfferingId`
- `@CanAccessEnrollment`: admin, the offering's instructor, or the enrolled student
- `@CurrentStudent`: `#studentId` is the logged-in student

The annotations answer "is this user involved with this resource at all?" using repo `exists` queries. The action policies answer "may they perform this specific action?". The relationship facts appear in both places on purpose, and `EnrollmentAuthorizationTest.AnnotationPolicyConsistency` keeps the two in agreement.

`CurrentUserService` reads the current `UserPrincipal` from the security context (id and role need no extra query) and throws `AuthenticationCredentialsNotFoundException` when there is none, including for anonymous requests on public routes.

### Error handling

Throw the domain exceptions from `common/exceptions`: `ResourceNotFoundException` (404), `BadRequestException` (400), `ForbiddenException` (403), `ConflictException` (409). `GlobalExceptionHandler` turns them, and Spring's own exceptions (validation, type mismatch, unreadable body, unknown route, wrong method, auth failures), into `ApiErrorResponse` with a message the UI shows as-is. So write messages for end users. Client errors are logged at `WARN` without a stack trace; unexpected errors at `ERROR` with one.

### API response pattern

All controllers return `ResponseEntity<ApiResponse<T>>` (success) or `ResponseEntity<ApiErrorResponse<T>>` (error) via the `Res` factory class. Use `Res.success(SuccessCode.CREATED, body)` / `Res.error(ErrorCode.X, message)` rather than constructing responses manually. Return flat lists of resources (for example, `GET /courses` returns `List<CourseResponse>`, each with its department); grouping for display is the client's job.

### Mappers

Each domain package has a `*Mapper` class with static methods for converting between entities, DTOs, and internal command objects. No MapStruct; all mappings are hand-written.

### Time

Inject the `Clock` bean (`TimeConfig`) and use `LocalDate.now(clock)`; never call `LocalDate.now()` directly. Tests replace it with a fixed clock (`TestClockConfig`), which keeps date-dependent logic such as registration windows deterministic.

### Package layout

Packages are by domain. Each holds its own controller / service / repo / entity / mapper, with `dto/`, `types/` (enums) and, where needed, `converter/` and `validators/` sub-packages.

```
auth/            — login, /auth/me, JWT issuance, AuthorizationExpressions constants
admin/           — admin registration-approval controllers, AdminSeeder
registration/    — student/ and instructor/ registration requests
user/            — UserEntity, UserService, CurrentUserService
student/         — StudentEntity, StudentService
instructor/      — InstructorEntity, InstructorService, InstructorValidator
department/      — DepartmentEntity, public GET /departments
course/          — CourseEntity, GET/POST /courses
semester/        — SemesterEntity, SemesterStatus/Action, SemesterActionPolicy, SemesterValidator
courseOffering/  — CourseOfferingEntity (capacity / enrolled seats), offerings and their enrollments
enrollment/      — EnrollmentEntity, EnrollmentStatus/Action/Denial, EnrollmentActionPolicy
security/        — SecurityConfig, jwt/, UserPrincipal, AuthorizationService, annotation/
config/          — JpaConfig, AppSecurityBeansConfig, PublicEndpointConfig, TimeConfig
common/          — rest (Res, ApiResponse, ErrorCode, SuccessCode), exceptions, entity (BaseEntity), types
```

### Database

Flyway owns the schema in every profile. Migrations live in `src/main/resources/db/migration/` (`V1__initial_schema.sql` … `V5__courseOffering_enrolled.sql`), and `ddl-auto=validate` makes Hibernate check the entity mappings against them. Schema changes always go in a new `V{n}__description.sql`; never edit an applied migration.

JPA uses `PhysicalNamingStrategyStandardImpl`, so column and table names match exactly what you write in the entity (no automatic camelCase → snake_case conversion).

Invariants are enforced twice: in code with a clear message, and in the database as a backstop. Examples are unique semester `term + year`, unique course `code`, unique offering `course + semester + section`, the `chk_*` date, credits and seat checks, and `0 <= enrolled <= capacity`.

## Testing

Tests run against MySQL in Testcontainers (`MySqlTestContainer`), so Docker must be running.

- **Base classes** (`config/`): `IntegrationTests` (`@SpringBootTest`, rolled back per test) and `RepoTests` (`@DataJpaTest`). Concurrency tests extend `MySqlTestContainer` directly without `@Transactional`, because threads must see each other's commits; they clean up in `@AfterEach`.
- **Test data** (`testUtils/`): `fixtures/` (pre-filled entity and request builders), `seeders/` (save fixtures, with counters for unique values), `scenario/` (builders that seed a whole graph, e.g. `EnrollmentScenarioSeeder`). Use these; don't build ad-hoc helpers.
- **Authentication:** `RoleActor` (`ADMIN`, `INSTRUCTOR`, `STUDENT`, `ANONYMOUS`, each with `authenticate()`) and `AuthOutcome` (with `expectedException()`) for role-only authorization tables; `TestAuthentication.asAdmin()`, `asRole(Role)`, `asUser(UserEntity)`, `asStudent(...)`, `asInstructor(...)` and `clear()`, or the `@WithAdmin` / `@WithInstructor` / `@WithStudent` annotations. Clear the context in `@AfterEach` when setting it by hand.
- **Style:** prefer `@ParameterizedTest` tables. Transition tests list the valid cases and derive the invalid ones as "all pairs minus valid". Authorization tests are actor × outcome tables (`ALLOWED`, `ACCESS_DENIED`, `UNAUTHENTICATED`) that start from a state where the action is otherwise valid, so a denial can only come from authorization.
- Controller (MockMvc) tests are not written yet.
