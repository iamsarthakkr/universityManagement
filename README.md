# University Enrollment Management System

A backend-focused university enrollment platform built with **Spring Boot, JPA, MySQL, and Spring Security**.

The project goes beyond basic CRUD by modelling real enrollment workflows including semester registration windows, course offerings, role/resource-based authorization, enrollment state transitions, capacity limits, transactional consistency, and concurrent enrollment handling.

## Features

- Student and instructor registration
- Department management
- Course catalogue
- Semester lifecycle management
- Registration windows
- Course offerings with instructors and capacity
- Enrollment request, approval, cancellation, and drop workflows
- Role-based and resource-level authorization
- Database migrations with Flyway
- Integration and repository testing
- Concurrency-safe enrollment approval
- Database-level duplicate enrollment protection

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

## Enrollment Workflow

Enrollment is treated as a domain workflow rather than a simple many-to-many relationship.

Typical lifecycle:

```text
PENDING
   │
   ├── approve ──► APPROVED ──► DROPPED
   │
   └── cancel ───► CANCELLED
```

Before an enrollment is created or approved, the system validates conditions such as:

- registration window availability
- student eligibility
- duplicate enrollment
- course offering state
- available capacity
- valid enrollment status transition

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

Authorization combines **role-based access** with **resource ownership**.

Examples:

```text
ADMIN
└── access administrative resources

INSTRUCTOR
└── access enrollment/offering resources they teach

STUDENT
└── access enrollment resources belonging to them
```

Spring Security method-level authorization and reusable custom authorization rules are used to avoid relying only on controller-level role checks.

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

Pending enrollments do not reserve seats.

Capacity is enforced when the enrollment transitions to the approved state.

### Database Constraints as Final Guarantees

Application validation provides meaningful domain errors, while database constraints protect invariants under concurrent execution.

### Flyway for Schema Management

Database schema changes are versioned through Flyway migrations instead of relying on automatic production schema generation by Hibernate.

### Explicit Fetching

JPA relationships are generally lazy.

Where related data is required, explicit fetching strategies such as `JOIN FETCH`, entity graphs, or DTO queries are preferred to avoid unnecessary object graphs and N+1 queries.

Open Session in View is disabled.

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

Concurrency-sensitive enrollment operations also have dedicated test coverage.

## Project Structure

```text
universityManagement/
├── frontend/
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
department/
student/
instructor/
course/
semester/
courseOffering/
enrollment/
registration/
security/
```

## Running the Backend

### Requirements

- Java 21
- MySQL
- Git

Clone the repository:

```bash
git clone https://github.com/iamsarthakkr/universityManagement.git
cd universityManagement/server
```

Run the application:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw clean verify
```

## Database Migrations

Flyway migration files are stored under:

```text
server/src/main/resources/db/migration/
```

Schema evolution is version-controlled and applied automatically during application startup.

## Infrastructure

The next stage of the project focuses on production readiness:

- Docker
- Docker Compose
- GitHub Actions CI
- automated Docker image publishing
- VPS deployment
- Nginx reverse proxy
- HTTPS
- automated deployment pipeline

## Repository

https://github.com/iamsarthakkr/universityManagement
