package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.admin.AdminUser;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.course.CourseRepo;
import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.department.DepartmentRepo;
import com.sarthak.universityManagement.enrollment.EnrollmentEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.seed.dto.SeedFile;
import com.sarthak.universityManagement.semester.SemesterRepo;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SeedApplierTests extends IntegrationTests {
    @Autowired
    private SeedApplier seedApplier;
    @Autowired
    private SeedTracking seedTracking;
    @Autowired
    private SeedFileReader seedFileReader;
    @Autowired
    private AdminUser adminUser;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private InstructorRepo instructorRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private SemesterRepo semesterRepo;
    @Autowired
    private CourseOfferingRepo courseOfferingRepo;
    @Autowired
    private EnrollmentRepo enrollmentRepo;
    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;

    @Nested
    class ExampleFile {

        @Test
        void shouldSeedEveryRecordOfTheExampleFile() {
            var file = seedFileReader.parse(SeedFiles.example());

            var result = seedApplier.apply(SeedFiles.example(), true);

            assertTrue(result.applied());
            assertEquals(file.departments().size(), departmentRepo.count());
            assertEquals(file.instructors().size() + file.students().size(), userRepo.count());
            assertEquals(file.instructors().size(), instructorRepo.count());
            assertEquals(file.students().size(), studentRepo.count());
            assertEquals(file.courses().size(), courseRepo.count());
            assertEquals(file.semesters().size(), semesterRepo.count());
            assertEquals(file.courseOfferings().size(), courseOfferingRepo.count());
            assertEquals(file.enrollments().size(), enrollmentRepo.count());
            // Requests that never became an account, plus the APPROVED one behind every account
            assertEquals(
                file.studentRegistrations().size() + file.students().stream().filter(st -> st.registration() != null).count(),
                studentRegistrationRepo.count()
            );
            assertEquals(
                file.instructorRegistrations().size() + file.instructors().stream().filter(i -> i.registration() != null).count(),
                instructorRegistrationRepo.count()
            );
        }

        @Test
        void shouldShowEveryStateTheUiCanDisplay() {
            var file = seedFileReader.parse(SeedFiles.example());

            assertEquals(
                Set.of(SemesterStatus.COMPLETED, SemesterStatus.ACTIVE, SemesterStatus.PLANNED),
                file.semesters().stream().map(SeedFile.Semester::status).collect(Collectors.toSet())
            );
            assertTrue(file.semesters().stream().filter(sem -> sem.status() == SemesterStatus.COMPLETED).count() >= 2);
            assertEquals(
                Set.of(EnrollmentStatus.values()),
                file.enrollments().stream().map(SeedFile.Enrollment::status).collect(Collectors.toSet())
            );
            assertEquals(
                Set.of(RegistrationStatus.PENDING, RegistrationStatus.REJECTED),
                file.studentRegistrations().stream().map(SeedFile.StudentRegistration::status).collect(Collectors.toSet())
            );
            assertTrue(file.students().stream().allMatch(st -> st.registration() != null));
            assertTrue(file.instructors().stream().allMatch(i -> i.registration() != null));
        }

        @Test
        void shouldDeriveEnrolledSeatsFromEnrolledRows() {
            seedApplier.apply(SeedFiles.example(), true);

            var enrollments = enrollmentRepo.findAll();
            for (var offering : courseOfferingRepo.findAll()) {
                var enrolledRows = enrollments.stream()
                    .filter(e -> e.getCourseOffering().getId().equals(offering.getId()))
                    .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                    .count();
                assertEquals(enrolledRows, offering.getEnrolled().longValue(), "offering " + offering.getId());
            }
        }

        @Test
        void shouldResolveRelativeDatesFromTheDayItIsApplied() {
            // MutableTestClock starts at 2026-01-10
            seedApplier.apply(SeedFiles.example(), true);

            var current = semesterRepo.findByTermAndYear(SemesterTerm.WINTER, 2026).orElseThrow();
            assertEquals(LocalDate.of(2025, 12, 26), current.getRegistrationStartDate());
            assertEquals(LocalDate.of(2026, 1, 25), current.getRegistrationEndDate());
            assertEquals(LocalDate.of(2026, 1, 3), current.getStartDate());
            assertEquals(SemesterStatus.ACTIVE, current.getStatus());
            assertTrue(current.isRegistrationOpen(LocalDate.of(2026, 1, 10)));

            assertTrue(semesterRepo.findByTermAndYear(SemesterTerm.SUMMER, 2025).isPresent());
            assertTrue(semesterRepo.findByTermAndYear(SemesterTerm.SUMMER, 2026).isPresent());

            assertEquals(LocalDate.of(2026, 1, 10), seedTracking.findActiveRun().orElseThrow().anchorDate());
        }

        @Test
        void shouldEncodePasswords() {
            seedApplier.apply(SeedFiles.example(), true);

            var user = userRepo.findByUsername("ada.lovelace").orElseThrow();
            assertNotEquals("password", user.getPassword());
            assertTrue(passwordEncoder.matches("password", user.getPassword()));
            assertEquals(Role.INSTRUCTOR, user.getRole());
        }

        @Test
        void shouldGiveEveryAccountAMatchingApprovedRegistration() {
            var file = seedFileReader.parse(SeedFiles.example());
            seedApplier.apply(SeedFiles.example(), true);

            var riya = file.students().stream().filter(st -> st.username().equals("riya.sharma")).findFirst().orElseThrow();
            var user = userRepo.findByUsername("riya.sharma").orElseThrow();
            var student = studentRepo.findByUserId(user.getId()).orElseThrow();
            var registration = studentRegistration("riya.sharma", RegistrationStatus.APPROVED);

            assertEquals(user.getEmail(), registration.getEmail());
            assertEquals(user.getPassword(), registration.getPassword());
            assertEquals(student.getDepartment().getId(), registration.getDepartment().getId());
            assertEquals(student.getFirstName(), registration.getFirstName());
            assertEquals(student.getDateOfBirth(), registration.getDateOfBirth());
            assertEquals(noonUtc(riya.registration().requestedAt()), registration.getCreatedAt());
            assertEquals(noonUtc(riya.registration().reviewedAt()), registration.getReviewedAt());
            assertEquals(noonUtc(riya.registration().reviewedAt()), registration.getUpdatedAt());
            // No admin exists in tests, so nobody is recorded as the reviewer
            assertNull(registration.getReviewedBy());

            var instructorRegistrations = instructorRegistrationRepo.findAll();
            for (var instructor : file.instructors()) {
                assertEquals(1, instructorRegistrations.stream()
                    .filter(r -> r.getUsername().equals(instructor.username()))
                    .filter(r -> r.getRegistrationStatus() == RegistrationStatus.APPROVED)
                    .count(), instructor.username());
            }
        }

        @Test
        void shouldRecordTheAdminAsTheReviewerWhenThereIsOne() {
            var admin = userSeeder.seedOrGet(Role.ADMIN, adminUser.getUsername());

            seedApplier.apply(SeedFiles.example(), true);

            assertEquals(admin.getId(), studentRegistration("riya.sharma", RegistrationStatus.APPROVED).getReviewedBy().getId());
            var rejected = studentRegistrationRepo.findAll().stream()
                .filter(r -> r.getRegistrationStatus() == RegistrationStatus.REJECTED)
                .findFirst().orElseThrow();
            assertEquals(admin.getId(), rejected.getReviewedBy().getId());
        }

        @Test
        void shouldSeedRequestsWithTheirStatusAndDates() {
            var file = seedFileReader.parse(SeedFiles.example());
            seedApplier.apply(SeedFiles.example(), true);

            for (var request : file.studentRegistrations()) {
                var saved = studentRegistration(request.username(), request.status());
                assertEquals(noonUtc(request.requestedAt()), saved.getCreatedAt(), request.username());
                assertTrue(passwordEncoder.matches(request.password(), saved.getPassword()));
                if (request.status() == RegistrationStatus.PENDING) {
                    assertNull(saved.getReviewedAt());
                    assertNull(saved.getReviewedBy());
                } else {
                    assertEquals(noonUtc(request.reviewedAt()), saved.getReviewedAt(), request.username());
                }
            }
        }

        @Test
        void shouldBackdateEnrollmentsToTheirRequest() {
            var file = seedFileReader.parse(SeedFiles.example());
            seedApplier.apply(SeedFiles.example(), true);

            var oldest = file.enrollments().getFirst();
            var student = studentRepo.findByUserId(userRepo.findByUsername(oldest.student()).orElseThrow().getId()).orElseThrow();
            var saved = enrollmentRepo.findAll().stream()
                .filter(e -> e.getStudent().getId().equals(student.getId()))
                .min(Comparator.comparing(EnrollmentEntity::getCreatedAt))
                .orElseThrow();

            assertEquals(noonUtc(oldest.requestedAt()), saved.getCreatedAt());
        }
    }

    // The test clock is fixed at 2026-01-10
    private static Instant noonUtc(String offset) {
        return RelativeDates.resolve(offset, LocalDate.of(2026, 1, 10), "test").atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC);
    }

    private StudentRegistrationEntity studentRegistration(String username, RegistrationStatus status) {
        return studentRegistrationRepo.findAll().stream()
            .filter(r -> r.getUsername().equals(username) && r.getRegistrationStatus() == status)
            .findFirst()
            .orElseThrow(() -> new AssertionError("no " + status + " registration for " + username));
    }

    @Nested
    class ActiveRun {

        @Test
        void shouldDoNothingWhenTheSameFileIsAppliedAgain() {
            var first = seedApplier.apply(SeedFiles.example(), true);
            var second = seedApplier.apply(SeedFiles.example(), true);

            assertFalse(second.applied());
            assertEquals(first.runId(), second.runId());
            assertEquals(2, departmentRepo.count());
        }

        @Test
        void shouldNotApplyAChangedFileWhileARunIsActive() {
            var first = seedApplier.apply(SeedFiles.example(), true);
            var second = seedApplier.apply(SeedFiles.json(SeedFiles.minimal("NEW")), true);

            assertFalse(second.applied());
            assertEquals(first.runId(), second.runId());
            assertTrue(departmentRepo.findByCode("NEW").isEmpty());
        }
    }

    @Nested
    class Rejection {

        @Test
        void shouldRejectExamplePasswordsUnlessAllowed() {
            var exception = assertThrows(SeedException.class, () -> seedApplier.apply(SeedFiles.example(), false));

            assertTrue(exception.getMessage().contains("ada.lovelace"));
            assertEquals(0, departmentRepo.count());
        }

        @Test
        void shouldRejectAUsernameThatAlreadyExists() {
            userSeeder.seedOrGet(Role.ADMIN, "ada.lovelace");

            var exception = assertThrows(SeedException.class, () -> seedApplier.apply(SeedFiles.example(), true));

            assertTrue(exception.getMessage().contains("ada.lovelace"));
            assertEquals(0, departmentRepo.count());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.sarthak.universityManagement.seed.SeedApplierTests#invalidFiles")
        void shouldRejectInvalidFilesWithAReadableMessage(String name, String json, String expectedMessage) {
            var exception = assertThrows(SeedException.class, () -> seedApplier.apply(SeedFiles.json(json), true));

            assertTrue(
                exception.getMessage().contains(expectedMessage),
                () -> "expected '" + expectedMessage + "' in: " + exception.getMessage()
            );
            assertTrue(seedTracking.findActiveRun().isEmpty());
        }
    }

    static Stream<Arguments> invalidFiles() {
        return Stream.of(
            Arguments.of("wrong format version",
                """
                { "formatVersion": 99 }
                """,
                "formatVersion 99"),
            Arguments.of("unknown field",
                """
                { "formatVersion": 2, "departments": [ { "code": "A", "name": "A", "head": "x" } ] }
                """,
                "head"),
            Arguments.of("missing required field",
                """
                { "formatVersion": 2, "departments": [ { "code": "A" } ] }
                """,
                "departments[0].name"),
            Arguments.of("duplicate key",
                """
                { "formatVersion": 2, "departments": [ { "code": "A", "name": "A" }, { "code": "A", "name": "B" } ] }
                """,
                "Duplicate department code 'A'"),
            Arguments.of("unknown reference",
                """
                { "formatVersion": 2,
                  "departments": [ { "code": "A", "name": "A" } ],
                  "courses": [ { "code": "C1", "department": "XYZ", "title": "t", "description": "d", "credits": 3 } ] }
                """,
                "courses[C1].department: unknown department 'XYZ'"),
            Arguments.of("bad date",
                """
                { "formatVersion": 2, "semesters": [ { "key": "s", "term": "WINTER", "status": "PLANNED",
                  "registrationStartDate": "soon", "registrationEndDate": "+5d", "startDate": "+10d", "endDate": "+90d" } ] }
                """,
                "semesters[s].registrationStartDate: 'soon'"),
            Arguments.of("dates out of order",
                """
                { "formatVersion": 2, "semesters": [ { "key": "s", "term": "WINTER", "status": "PLANNED",
                  "registrationStartDate": "+1d", "registrationEndDate": "+5d", "startDate": "+90d", "endDate": "+10d" } ] }
                """,
                "startDate must be before endDate"),
            Arguments.of("more enrolled than capacity",
                """
                { "formatVersion": 2,
                  "departments": [ { "code": "A", "name": "A" } ],
                  "instructors": [ { "username": "i1", "email": "i1@example.com", "password": "pw", "department": "A", "firstName": "I" } ],
                  "students": [
                    { "username": "s1", "email": "s1@example.com", "password": "pw", "department": "A", "firstName": "S", "dateOfBirth": "2004-01-01" },
                    { "username": "s2", "email": "s2@example.com", "password": "pw", "department": "A", "firstName": "S", "dateOfBirth": "2004-01-01" } ],
                  "courses": [ { "code": "C1", "department": "A", "title": "t", "description": "d", "credits": 3 } ],
                  "semesters": [ { "key": "s", "term": "WINTER", "status": "PLANNED",
                    "registrationStartDate": "-1d", "registrationEndDate": "+5d", "startDate": "+10d", "endDate": "+90d" } ],
                  "courseOfferings": [ { "key": "o", "course": "C1", "semester": "s", "instructor": "i1", "section": "A", "capacity": 1 } ],
                  "enrollments": [
                    { "student": "s1", "offering": "o", "status": "ENROLLED" },
                    { "student": "s2", "offering": "o", "status": "ENROLLED" } ] }
                """,
                "exceed its capacity of 1"),
            Arguments.of("username used twice",
                """
                { "formatVersion": 2,
                  "departments": [ { "code": "A", "name": "A" } ],
                  "instructors": [ { "username": "same", "email": "i1@example.com", "password": "pw", "department": "A", "firstName": "I" } ],
                  "instructorRegistrations": [ { "status": "PENDING", "username": "same", "email": "i2@example.com", "password": "pw", "department": "A", "firstName": "I" } ] }
                """,
                "Username 'same'"),
            Arguments.of("approved request without an account",
                REQUEST.formatted("\"status\": \"APPROVED\", \"requestedAt\": \"-5d\", \"reviewedAt\": \"-4d\""),
                "an APPROVED request must match an account"),
            Arguments.of("rejected request without reviewedAt",
                REQUEST.formatted("\"status\": \"REJECTED\", \"requestedAt\": \"-5d\""),
                "a REJECTED request needs reviewedAt"),
            Arguments.of("pending request with reviewedAt",
                REQUEST.formatted("\"status\": \"PENDING\", \"requestedAt\": \"-5d\", \"reviewedAt\": \"-4d\""),
                "a PENDING request has no reviewedAt"),
            Arguments.of("reviewed before requested",
                REQUEST.formatted("\"status\": \"REJECTED\", \"requestedAt\": \"-5d\", \"reviewedAt\": \"-6d\""),
                "studentRegistrations[r1]: reviewedAt is before requestedAt"),
            Arguments.of("account reviewed before requested",
                """
                { "formatVersion": 2,
                  "departments": [ { "code": "A", "name": "A" } ],
                  "instructors": [ { "username": "i1", "email": "i1@example.com", "password": "pw", "department": "A", "firstName": "I",
                    "registration": { "requestedAt": "-5d", "reviewedAt": "-9d" } } ] }
                """,
                "instructors[i1].registration: reviewedAt is before requestedAt")
        );
    }

    private static final String REQUEST = """
        { "formatVersion": 2,
          "departments": [ { "code": "A", "name": "A" } ],
          "studentRegistrations": [ { %s, "username": "r1", "email": "r1@example.com", "password": "pw",
            "department": "A", "firstName": "R", "dateOfBirth": "2005-01-01" } ] }
        """;
}
