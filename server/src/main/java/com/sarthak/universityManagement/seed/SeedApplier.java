package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.admin.AdminUser;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.course.CourseRepo;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.department.DepartmentRepo;
import com.sarthak.universityManagement.enrollment.EnrollmentEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.seed.dto.SeedFile;
import com.sarthak.universityManagement.seed.types.SeedTable;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.SemesterRepo;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inserts a seed file's rows through the repositories, not the use-case services: those are guarded by
 * {@code @PreAuthorize} and only allow step-by-step transitions, while demo data starts in its final state
 * (a COMPLETED semester, DROPPED enrollments). Database constraints still back every insert.
 */
@Service
public class SeedApplier {
    private static final Logger LOG = LogManager.getLogger(SeedApplier.class);
    static final String EXAMPLE_PASSWORD = "password";

    private final SeedFileReader seedFileReader;
    private final SeedTracking seedTracking;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final DepartmentRepo departmentRepo;
    private final UserRepo userRepo;
    private final InstructorRepo instructorRepo;
    private final StudentRepo studentRepo;
    private final CourseRepo courseRepo;
    private final SemesterRepo semesterRepo;
    private final CourseOfferingRepo courseOfferingRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final AdminUser adminUser;
    private final NamedParameterJdbcTemplate jdbc;
    private final EntityManager entityManager;

    public SeedApplier(
        SeedFileReader seedFileReader,
        SeedTracking seedTracking,
        PasswordEncoder passwordEncoder,
        Clock clock,
        DepartmentRepo departmentRepo,
        UserRepo userRepo,
        InstructorRepo instructorRepo,
        StudentRepo studentRepo,
        CourseRepo courseRepo,
        SemesterRepo semesterRepo,
        CourseOfferingRepo courseOfferingRepo,
        EnrollmentRepo enrollmentRepo,
        StudentRegistrationRepo studentRegistrationRepo,
        InstructorRegistrationRepo instructorRegistrationRepo,
        AdminUser adminUser,
        NamedParameterJdbcTemplate jdbc,
        EntityManager entityManager
    ) {
        this.seedFileReader = seedFileReader;
        this.seedTracking = seedTracking;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.departmentRepo = departmentRepo;
        this.userRepo = userRepo;
        this.instructorRepo = instructorRepo;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.semesterRepo = semesterRepo;
        this.courseOfferingRepo = courseOfferingRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.studentRegistrationRepo = studentRegistrationRepo;
        this.instructorRegistrationRepo = instructorRegistrationRepo;
        this.adminUser = adminUser;
        this.jdbc = jdbc;
        this.entityManager = entityManager;
    }

    public record ApplyResult(int runId, boolean applied) {}

    /**
     * Applies the file unless a seed run is already active, in which case nothing changes: re-seeding over
     * existing seed data would collide on unique keys. To change the data, roll back first.
     * Everything happens in one transaction, so a bad reference leaves the database untouched.
     */
    @Transactional
    public ApplyResult apply(byte[] content, boolean allowExamplePasswords) {
        var checksum = SeedFileReader.checksum(content);
        var activeRun = seedTracking.findActiveRun();
        if (activeRun.isPresent()) {
            var run = activeRun.get();
            if (run.checksum().equals(checksum)) {
                LOG.info("Seed already applied as run {} on {}; nothing to do", run.id(), run.appliedAt());
            } else {
                LOG.warn(
                    "Seed run {} (applied {}) is active and the seed file has changed since. "
                        + "Set app.seed.mode=rollback, restart, then apply again.",
                    run.id(), run.appliedAt()
                );
            }
            return new ApplyResult(run.id(), false);
        }

        var seedFile = seedFileReader.parse(content);
        if (!allowExamplePasswords) {
            rejectExamplePasswords(seedFile);
        }
        rejectTakenCredentials(seedFile);

        var anchor = LocalDate.now(clock);
        // AdminSeeder runs first, so the admin exists at startup; without one (tests) reviews have no reviewer
        var reviewer = userRepo.findByUsername(adminUser.getUsername()).orElse(null);
        var inserted = new Inserted(new SeedContext(anchor, reviewer));
        insertAll(seedFile, inserted);
        backdate(inserted.backdates);

        var runId = seedTracking.startRun(SeedFile.CURRENT_FORMAT_VERSION, checksum, anchor, Instant.now(clock));
        inserted.ids.forEach((table, ids) -> seedTracking.recordRows(runId, table, ids));

        LOG.info("Seed run {} applied with anchor date {}: {}", runId, anchor, inserted.summary());
        return new ApplyResult(runId, true);
    }

    private void insertAll(SeedFile file, Inserted inserted) {
        var ctx = inserted.ctx;

        for (var department : file.departments()) {
            ctx.requireUnused(ctx.departments, department.code(), "department code");
            var saved = departmentRepo.save(DepartmentEntity.builder()
                .code(department.code())
                .name(department.name())
                .build());
            ctx.departments.put(department.code(), saved);
            inserted.add(SeedTable.DEPARTMENT, saved.getId());
        }

        for (var instructor : file.instructors()) {
            var where = "instructors[" + instructor.username() + "]";
            var user = saveUser(instructor.username(), instructor.email(), instructor.password(), Role.INSTRUCTOR, inserted);
            ctx.requireUnused(ctx.instructors, instructor.username(), "instructor username");
            var saved = instructorRepo.save(InstructorEntity.builder()
                .user(user)
                .department(ctx.get(ctx.departments, instructor.department(), where + ".department", "department"))
                .firstName(instructor.firstName())
                .lastName(instructor.lastName())
                .phoneNumber(instructor.phoneNumber())
                .build());
            ctx.instructors.put(instructor.username(), saved);
            inserted.add(SeedTable.INSTRUCTOR, saved.getId());

            if (instructor.registration() != null) {
                var review = Review.approved(instructor.registration(), ctx, where + ".registration");
                var registration = instructorRegistrationRepo.save(InstructorRegistrationEntity.builder()
                    .registrationStatus(RegistrationStatus.APPROVED)
                    .department(saved.getDepartment())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .firstName(saved.getFirstName())
                    .lastName(saved.getLastName())
                    .reviewedAt(review.reviewedAt())
                    .reviewedBy(ctx.reviewer)
                    .build());
                inserted.add(SeedTable.INSTRUCTOR_REGISTRATION, registration.getId());
                inserted.backdate(SeedTable.INSTRUCTOR_REGISTRATION, registration.getId(), review);
            }
        }

        for (var student : file.students()) {
            var where = "students[" + student.username() + "]";
            var user = saveUser(student.username(), student.email(), student.password(), Role.STUDENT, inserted);
            ctx.requireUnused(ctx.students, student.username(), "student username");
            var saved = studentRepo.save(StudentEntity.builder()
                .user(user)
                .department(ctx.get(ctx.departments, student.department(), where + ".department", "department"))
                .firstName(student.firstName())
                .lastName(student.lastName())
                .dateOfBirth(student.dateOfBirth())
                .phoneNumber(student.phoneNumber())
                .address(student.address())
                .fatherName(student.fatherName())
                .motherName(student.motherName())
                .build());
            ctx.students.put(student.username(), saved);
            inserted.add(SeedTable.STUDENT, saved.getId());

            if (student.registration() != null) {
                var review = Review.approved(student.registration(), ctx, where + ".registration");
                var registration = studentRegistrationRepo.save(StudentRegistrationEntity.builder()
                    .registrationStatus(RegistrationStatus.APPROVED)
                    .department(saved.getDepartment())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .firstName(saved.getFirstName())
                    .lastName(saved.getLastName())
                    .dateOfBirth(saved.getDateOfBirth())
                    .reviewedAt(review.reviewedAt())
                    .reviewedBy(ctx.reviewer)
                    .build());
                inserted.add(SeedTable.STUDENT_REGISTRATION, registration.getId());
                inserted.backdate(SeedTable.STUDENT_REGISTRATION, registration.getId(), review);
            }
        }

        for (var course : file.courses()) {
            var where = "courses[" + course.code() + "]";
            ctx.requireUnused(ctx.courses, course.code(), "course code");
            var saved = courseRepo.save(CourseEntity.builder()
                .code(course.code())
                .department(ctx.get(ctx.departments, course.department(), where + ".department", "department"))
                .title(course.title())
                .description(course.description())
                .credits(course.credits())
                .build());
            ctx.courses.put(course.code(), saved);
            inserted.add(SeedTable.COURSE, saved.getId());
        }

        for (var semester : file.semesters()) {
            ctx.requireUnused(ctx.semesters, semester.key(), "semester key");
            var saved = semesterRepo.save(toSemester(semester, ctx.anchor));
            ctx.semesters.put(semester.key(), saved);
            inserted.add(SeedTable.SEMESTER, saved.getId());
        }

        // enrolled is derived from the ENROLLED rows, never written in the file, so the two cannot disagree
        var enrolledByOffering = file.enrollments().stream()
            .filter(enrollment -> enrollment.status() == EnrollmentStatus.ENROLLED)
            .collect(Collectors.groupingBy(SeedFile.Enrollment::offering, Collectors.counting()));

        for (var offering : file.courseOfferings()) {
            var where = "courseOfferings[" + offering.key() + "]";
            var enrolled = enrolledByOffering.getOrDefault(offering.key(), 0L).intValue();
            if (enrolled > offering.capacity()) {
                throw new SeedException(
                    where + ": " + enrolled + " ENROLLED enrollments exceed its capacity of " + offering.capacity()
                );
            }
            ctx.requireUnused(ctx.offerings, offering.key(), "course offering key");
            var saved = courseOfferingRepo.save(CourseOfferingEntity.builder()
                .course(ctx.get(ctx.courses, offering.course(), where + ".course", "course"))
                .semester(ctx.get(ctx.semesters, offering.semester(), where + ".semester", "semester key"))
                .instructor(ctx.get(ctx.instructors, offering.instructor(), where + ".instructor", "instructor"))
                .section(offering.section())
                .capacity(offering.capacity())
                .enrolled(enrolled)
                .build());
            ctx.offerings.put(offering.key(), saved);
            inserted.add(SeedTable.COURSE_OFFERING, saved.getId());
        }

        var seenEnrollments = new HashSet<String>();
        for (var enrollment : file.enrollments()) {
            var where = "enrollments[" + enrollment.student() + " -> " + enrollment.offering() + "]";
            if (!seenEnrollments.add(enrollment.student() + "|" + enrollment.offering())) {
                throw new SeedException(where + ": the student is enrolled in this offering twice");
            }
            var saved = enrollmentRepo.save(EnrollmentEntity.builder()
                .student(ctx.get(ctx.students, enrollment.student(), where + ".student", "student"))
                .courseOffering(ctx.get(ctx.offerings, enrollment.offering(), where + ".offering", "course offering key"))
                .status(enrollment.status())
                .build());
            inserted.add(SeedTable.ENROLLMENT, saved.getId());
            if (enrollment.requestedAt() != null) {
                var requestedAt = ctx.instant(enrollment.requestedAt(), where + ".requestedAt");
                inserted.backdate(SeedTable.ENROLLMENT, saved.getId(), new Review(requestedAt, null));
            }
        }

        for (var registration : file.studentRegistrations()) {
            var where = "studentRegistrations[" + registration.username() + "]";
            var review = Review.unapproved(registration.status(), registration.requestedAt(), registration.reviewedAt(), ctx, where);
            var saved = studentRegistrationRepo.save(StudentRegistrationEntity.builder()
                .registrationStatus(registration.status())
                .reviewedAt(review.reviewedAt())
                .reviewedBy(review.reviewedAt() == null ? null : ctx.reviewer)
                .department(ctx.get(ctx.departments, registration.department(), where + ".department", "department"))
                .username(registration.username())
                .email(registration.email())
                .password(passwordEncoder.encode(registration.password()))
                .firstName(registration.firstName())
                .lastName(registration.lastName())
                .dateOfBirth(registration.dateOfBirth())
                .build());
            inserted.add(SeedTable.STUDENT_REGISTRATION, saved.getId());
            inserted.backdate(SeedTable.STUDENT_REGISTRATION, saved.getId(), review);
        }

        for (var registration : file.instructorRegistrations()) {
            var where = "instructorRegistrations[" + registration.username() + "]";
            var review = Review.unapproved(registration.status(), registration.requestedAt(), registration.reviewedAt(), ctx, where);
            var saved = instructorRegistrationRepo.save(InstructorRegistrationEntity.builder()
                .registrationStatus(registration.status())
                .reviewedAt(review.reviewedAt())
                .reviewedBy(review.reviewedAt() == null ? null : ctx.reviewer)
                .department(ctx.get(ctx.departments, registration.department(), where + ".department", "department"))
                .username(registration.username())
                .email(registration.email())
                .password(passwordEncoder.encode(registration.password()))
                .firstName(registration.firstName())
                .lastName(registration.lastName())
                .build());
            inserted.add(SeedTable.INSTRUCTOR_REGISTRATION, saved.getId());
            inserted.backdate(SeedTable.INSTRUCTOR_REGISTRATION, saved.getId(), review);
        }
    }

    /*
     * JPA auditing stamps createdAt with "now" and never lets it change, so historical rows get their real
     * request dates here, after the inserts. updatedAt follows the review, as it would have in the app.
     */
    private void backdate(List<Backdate> backdates) {
        if (backdates.isEmpty()) {
            return;
        }
        entityManager.flush();
        var byTable = backdates.stream().collect(Collectors.groupingBy(Backdate::table));
        byTable.forEach((table, rows) -> jdbc.batchUpdate(
            "UPDATE " + table.getTableName() + " SET createdAt = :createdAt, updatedAt = :updatedAt WHERE id = :id",
            rows.stream().map(row -> Map.<String, Object>of(
                "id", row.id(),
                "createdAt", utcWallClock(row.review().requestedAt()),
                "updatedAt", utcWallClock(row.review().lastChangedAt())
            )).toArray(Map[]::new)
        ));
        // The managed entities still hold "now"; anything read later in this transaction must come from the database
        entityManager.clear();
    }

    // Hibernate stores an Instant as its UTC wall-clock time; Timestamp.from would use the JVM's zone and shift it
    private static LocalDateTime utcWallClock(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private UserEntity saveUser(String username, String email, String password, Role role, Inserted inserted) {
        var saved = userRepo.save(UserEntity.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(role)
            .active(true)
            .build());
        inserted.add(SeedTable.USERS, saved.getId());
        return saved;
    }

    private static SemesterEntity toSemester(SeedFile.Semester semester, LocalDate anchor) {
        var where = "semesters[" + semester.key() + "]";
        var registrationStart = RelativeDates.resolve(semester.registrationStartDate(), anchor, where + ".registrationStartDate");
        var registrationEnd = RelativeDates.resolve(semester.registrationEndDate(), anchor, where + ".registrationEndDate");
        var start = RelativeDates.resolve(semester.startDate(), anchor, where + ".startDate");
        var end = RelativeDates.resolve(semester.endDate(), anchor, where + ".endDate");

        if (!registrationStart.isBefore(registrationEnd)) {
            throw new SeedException(where + ": registrationStartDate must be before registrationEndDate");
        }
        if (!start.isBefore(end)) {
            throw new SeedException(where + ": startDate must be before endDate");
        }

        return SemesterEntity.builder()
            .term(semester.term())
            .year(start.getYear())
            .status(semester.status())
            .registrationStartDate(registrationStart)
            .registrationEndDate(registrationEnd)
            .startDate(start)
            .endDate(end)
            .build();
    }

    private record Review(Instant requestedAt, Instant reviewedAt) {
        Instant lastChangedAt() {
            return reviewedAt != null ? reviewedAt : requestedAt;
        }

        static Review approved(SeedFile.Registration registration, SeedContext ctx, String where) {
            return ordered(
                ctx.instant(registration.requestedAt(), where + ".requestedAt"),
                ctx.instant(registration.reviewedAt(), where + ".reviewedAt"),
                where
            );
        }

        static Review unapproved(RegistrationStatus status, String requestedAt, String reviewedAt, SeedContext ctx, String where) {
            switch (status) {
                case APPROVED -> throw new SeedException(
                    where + ": an APPROVED request must match an account; give the student or instructor a \"registration\" instead"
                );
                case PENDING -> {
                    if (reviewedAt != null) {
                        throw new SeedException(where + ": a PENDING request has no reviewedAt");
                    }
                }
                case REJECTED -> {
                    if (reviewedAt == null) {
                        throw new SeedException(where + ": a REJECTED request needs reviewedAt");
                    }
                }
            }
            var requested = requestedAt == null ? null : ctx.instant(requestedAt, where + ".requestedAt");
            var reviewed = reviewedAt == null ? null : ctx.instant(reviewedAt, where + ".reviewedAt");
            if (requested == null && reviewed != null) {
                throw new SeedException(where + ": reviewedAt needs a requestedAt");
            }
            return ordered(requested, reviewed, where);
        }

        private static Review ordered(Instant requested, Instant reviewed, String where) {
            if (requested != null && reviewed != null && reviewed.isBefore(requested)) {
                throw new SeedException(where + ": reviewedAt is before requestedAt");
            }
            return new Review(requested, reviewed);
        }
    }

    private record Backdate(SeedTable table, Integer id, Review review) {}

    private record Credential(String username, String email, String password) {}

    private static List<Credential> credentials(SeedFile file) {
        List<Credential> credentials = new ArrayList<>();
        file.instructors().forEach(i -> credentials.add(new Credential(i.username(), i.email(), i.password())));
        file.students().forEach(s -> credentials.add(new Credential(s.username(), s.email(), s.password())));
        file.studentRegistrations().forEach(r -> credentials.add(new Credential(r.username(), r.email(), r.password())));
        file.instructorRegistrations().forEach(r -> credentials.add(new Credential(r.username(), r.email(), r.password())));
        return credentials;
    }

    // Users and PENDING registrations both reserve a username/email (RegistrationValidator), so check across all of them
    private void rejectTakenCredentials(SeedFile file) {
        var usernames = new HashSet<String>();
        var emails = new HashSet<String>();
        for (var credential : credentials(file)) {
            if (!usernames.add(credential.username()) || userRepo.existsByUsername(credential.username())) {
                throw new SeedException(
                    "Username '" + credential.username() + "' is used twice in the seed file or already exists"
                );
            }
            if (!emails.add(credential.email()) || userRepo.existsByEmail(credential.email())) {
                throw new SeedException(
                    "Email '" + credential.email() + "' is used twice in the seed file or already exists"
                );
            }
        }
    }

    private static void rejectExamplePasswords(SeedFile file) {
        var offenders = credentials(file).stream()
            .filter(credential -> EXAMPLE_PASSWORD.equals(credential.password()))
            .map(Credential::username)
            .sorted()
            .toList();
        if (!offenders.isEmpty()) {
            throw new SeedException(
                "Seed file uses the example password '" + EXAMPLE_PASSWORD + "' for " + offenders
                    + ". It is only allowed with app.seed.allow-example-passwords=true (dev); "
                    + "use real passwords for anything reachable from the internet."
            );
        }
    }

    private static final class SeedContext {
        final LocalDate anchor;
        final UserEntity reviewer;
        final Map<String, DepartmentEntity> departments = new HashMap<>();
        final Map<String, InstructorEntity> instructors = new HashMap<>();
        final Map<String, StudentEntity> students = new HashMap<>();
        final Map<String, CourseEntity> courses = new HashMap<>();
        final Map<String, SemesterEntity> semesters = new HashMap<>();
        final Map<String, CourseOfferingEntity> offerings = new HashMap<>();

        SeedContext(LocalDate anchor, UserEntity reviewer) {
            this.anchor = anchor;
            this.reviewer = reviewer;
        }

        // Noon UTC keeps the calendar day the same in every timezone between UTC-11 and UTC+11
        Instant instant(String value, String field) {
            return RelativeDates.resolve(value, anchor, field).atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC);
        }

        // Checked before the insert, so a duplicate reports this message rather than a unique-constraint error
        void requireUnused(Map<String, ?> map, String key, String what) {
            if (map.containsKey(key)) {
                throw new SeedException("Duplicate " + what + " '" + key + "' in the seed file");
            }
        }

        <T> T get(Map<String, T> map, String key, String where, String what) {
            var value = map.get(key);
            if (value == null) {
                throw new SeedException(where + ": unknown " + what + " '" + key + "'");
            }
            return value;
        }
    }

    private static final class Inserted {
        final SeedContext ctx;
        final Map<SeedTable, List<Integer>> ids = new EnumMap<>(SeedTable.class);
        final List<Backdate> backdates = new ArrayList<>();

        Inserted(SeedContext ctx) {
            this.ctx = ctx;
        }

        void add(SeedTable table, Integer id) {
            ids.computeIfAbsent(table, t -> new ArrayList<>()).add(id);
        }

        void backdate(SeedTable table, Integer id, Review review) {
            if (review.requestedAt() != null) {
                backdates.add(new Backdate(table, id, review));
            }
        }

        String summary() {
            return ids.entrySet().stream()
                .map(entry -> entry.getKey().getTableName() + "=" + entry.getValue().size())
                .collect(Collectors.joining(", "));
        }
    }
}
