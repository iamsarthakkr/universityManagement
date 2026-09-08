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

`application.yml` contains the shared base config. The `dev` profile (`application-dev.yml`) connects to a local MySQL DB at `localhost:3306/universityManagementDev` with credentials `test/test`, and the `prod` profile (`application-prod.yml`) reads its sensitive values from environment variables. Set `SPRING_PROFILES_ACTIVE=dev` or `SPRING_PROFILES_ACTIVE=prod` (or pass `-Dspring-boot.run.profiles=...`) to activate a profile.

Required properties (all overridden in dev profile):
- `spring.datasource.url/username/password`
- `app.jwt.secret` — must be ≥256 bits
- `app.admin.username/password/email` — seeds the admin user on startup via `AdminSeeder`

## Architecture

### Domain model

Five core domain entities: `UserEntity`, `StudentEntity`, `InstructorEntity`, `CourseEntity`, plus registration workflow entities (`StudentRegistrationEntity`, `InstructorRegistrationEntity`). Roles are stored in `UserEntity` and used for authorization via Spring Security.

### Registration workflow

New users go through a two-step registration process:
1. Public POST to `/registration/student` or `/registration/instructor` creates a `RegistrationEntity` with status `PENDING`.
2. Admin approves/rejects via `/admin/registration/student/{id}/approve` (etc.), which triggers creation of the actual `UserEntity` and associated `StudentEntity`/`InstructorEntity`.

`StudentRegistrationService` and `InstructorRegistrationService` own this logic. `@PreAuthorize(AuthorizationExpressions.ADMIN)` guards all admin actions.

### Security

Stateless JWT auth. `JwtAuthenticationFilter` validates tokens on every request. `SecurityConfig` defines route-level rules; method-level rules use `@PreAuthorize` with expressions from `AuthorizationExpressions`. `CurrentUserService` resolves the authenticated principal to a `UserEntity`.

Public endpoints are declared via `PublicEndpointConfig` (a `RequestMatcher` bean), making them easy to extend without touching `SecurityConfig`.

### API response pattern

All controllers return `ResponseEntity<ApiResponse<T>>` (success) or `ResponseEntity<ApiErrorResponse<T>>` (error) via the `Res` factory class. Use `Res.success(SuccessCode.CREATED, body)` / `Res.error(ErrorCode.X, message)` rather than constructing responses manually.

### Mappers

Each domain package has a `*Mapper` class with static methods for converting between entities, DTOs, and internal command objects. No MapStruct — all mappings are hand-written.

### Package layout

```
auth/          — login endpoint, JWT token issuance, AuthorizationExpressions constants
admin/         — admin-only controllers for approving registrations, AdminSeeder
registration/  — student/ and instructor/ sub-packages with entity/repo/service/mapper/dto
student/       — StudentEntity, StudentService, StudentRepo, StudentMapper
instructor/    — InstructorEntity, InstructorService, InstructorRepo, InstructorMapper
user/          — UserEntity, UserService, UserRepo, CurrentUserService
course/        — CourseEntity, CourseService, CourseRepo, CourseMapper, CourseController
security/      — SecurityConfig, JwtAuthenticationFilter, JwtService, UserPrincipal, etc.
config/        — JpaConfig, AppSecurityBeansConfig, PublicEndpointConfig
common/        — rest (Res, ApiResponse, ErrorCode, SuccessCode), exceptions, types (Role, RegistrationStatus)
```

### Database

**Migration to Flyway is in progress.** Schema is now defined via versioned migrations in `src/main/resources/db/migration/` (e.g. `V1__initial_schema.sql`), replacing the old `src/main/resources/sql/*.sql` scripts (`schema.sql`, `admin.sql`, `reset.sql`), which have been deleted. `flyway-core` and `flyway-mysql` are on the classpath (`pom.xml`), so Flyway auto-runs migrations on startup by default in every profile unless explicitly disabled.

- `prod` profile: `spring.flyway.enabled=true`, `locations=classpath:db/migration`, `ddl-auto=validate` — Flyway owns the schema, Hibernate only validates entity mappings against it.
- `dev` profile: no Flyway override (so it inherits the default enabled behavior and runs the same migrations against `localhost:3306/universityManagementDev`), `ddl-auto=validate`, `sql.init.mode=never` — dev no longer uses `ddl-auto=update`; schema changes must go through a new migration file.

JPA uses `PhysicalNamingStrategyStandardImpl` so column/table names match exactly what you write in the entity (no automatic camelCase → snake_case conversion). When adding/changing entities, add a new `V{n}__description.sql` migration under `db/migration` rather than relying on Hibernate to generate the schema.
