# Server checklist

Backend changes the frontend is waiting on. Check off and delete items as they land.

## Allowed transitions in responses

The UI does not duplicate state-machine rules; it renders the actions the server says are allowed.

- [ ] `EnrollmentDetailResponse.allowedTransitions: List<EnrollmentStatus>` — filled in `EnrollmentMapper` from
      `EnrollmentEntity.canTransitionTo`. Drives Approve / Reject (`ENROLLED` / `REJECTED`) and the student's
      Cancel / Drop (`CANCELLED` / `DROPPED`). Returned by both `GET /enrollments/me` and
      `GET /course-offerings/{id}/enrollments`.

## Open items from the UI plan

- [ ] **Instructor's own offerings** (blocks the instructor "Assigned courses" page): add
      `GET /course-offerings/me` (resolve the instructor from the current user, like `/enrollments/me`), or expose
      `instructorId` on `/auth/me`.
- [ ] **Offerings allowed** (admin "Add offering" form): `SemesterValidator.validateSemesterAllowsOfferings`
      (status `PLANNED`) — consider exposing it as a flag on `SemesterResponse`.
- [ ] **Semester create validation:** `CreateSemesterRequest` only checks `@NotNull`. A duplicate term + year or
      out-of-order dates fall through to the DB constraints (`unique_semester_term_year`, `chk_semester_dates`) and
      come back as a generic 409 "Request violates a database constraint". Validate in `SemesterService` with
      specific messages (`ConflictException` for the duplicate, `BadRequestException` for the dates) — the UI shows
      the server message as-is. Also decide whether registration must close before the semester starts; nothing
      enforces it today.
- [ ] *(Optional)* `CourseOfferingResponse` carries only ids — adding `courseCode`, `courseTitle` and
      `instructorName` saves the UI from joining against the catalogue and instructors list.
- [ ] *(Optional)* Duplicate offering (`course + semester + section`) throws `BadRequestException` (400);
      `ConflictException` (409) fits better.
