# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Spring Boot 4.x (Java 21), Spring Security (JWT, stateless), Spring Data JPA, MySQL, Lombok, JUnit Jupiter 6.

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

`application.properties` contains placeholder values. The `dev` profile (`application-dev.properties`) connects to a local MySQL DB at `localhost:3306/universityManagementDev` with credentials `test/test`. Set `SPRING_PROFILES_ACTIVE=dev` or pass `-Dspring-boot.run.profiles=dev` to activate it.

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

Schema is defined in `src/main/resources/sql/schema.sql` and applied with `spring.sql.init.mode=always` in production. In the dev profile, `ddl-auto=update` is used instead and `sql.init.mode=never`. JPA uses `PhysicalNamingStrategyStandardImpl` so column/table names match exactly what you write in the entity (no automatic camelCase → snake_case conversion).
