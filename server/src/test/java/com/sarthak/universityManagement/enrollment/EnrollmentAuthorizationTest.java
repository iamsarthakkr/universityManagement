package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.ForbiddenException;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentAuthorizationTest extends IntegrationTests {
    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentScenarioSeeder studentScenarioSeeder;
    @Autowired
    private CourseOfferingScenarioSeeder courseOfferingScenarioSeeder;
    @Autowired
    private EnrollmentScenarioSeeder enrollmentScenarioSeeder;
    @Autowired
    private Clock clock;

    enum TestActor { ADMIN, OFFERING_INSTRUCTOR, OTHER_INSTRUCTOR, OWNING_STUDENT, OTHER_STUDENT }
    enum Outcome { ALLOWED, NOT_ALLOWED, ACCESS_DENIED }

    private void authenticateAs(TestActor actor, EnrollmentScenario target, EnrollmentScenario other) {
        switch (actor) {
            case ADMIN -> TestAuthentication.asAdmin();
            case OFFERING_INSTRUCTOR -> TestAuthentication.asInstructor(target.instructor());
            case OTHER_INSTRUCTOR -> TestAuthentication.asInstructor(other.instructor());
            case OWNING_STUDENT -> TestAuthentication.asStudent(target.student());
            case OTHER_STUDENT -> TestAuthentication.asStudent(other.student());
        }
    }

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    @Nested
    class CreationAuthorization {
        private StudentScenario studentScenario1, studentScenario2;
        private CourseOfferingScenarioSeeder.Scenario courseOfferingScenarioBuilder;

        @BeforeEach
        void setup() {
            studentScenario1 = studentScenarioSeeder.builder()
                .department(d -> d.departmentNumber(1))
                .studentNumber(1)
                .build();

            studentScenario2 = studentScenarioSeeder.builder()
                .department(d -> d.departmentNumber(1))
                .studentNumber(2)
                .build();

            courseOfferingScenarioBuilder = courseOfferingScenarioSeeder.builder()
                .departmentNumber(1)
                .courseNumber(1)
                .instructorNumber(1)
                .capacity(10)
                .enrolled(0)
                .semester(s -> s.registrationOpenOn(LocalDate.now(clock)));

        }

        @Test
        public void shouldAllowStudentToCreateEnrollmentForSelf() {
            var student =  studentScenario1.student();
            var offering = courseOfferingScenarioBuilder.build().courseOffering();

            TestAuthentication.asStudent(student);

            var res =  enrollmentService.createEnrollment(student.getId(), offering.getId());
            assertNotNull(res);

        }

        @Test
        public void shouldDenyStudentToCreateEnrollmentForAnotherStudent() {
            var student1 =  studentScenario1.student();
            var student2 =  studentScenario2.student();
            var offering = courseOfferingScenarioBuilder.build().courseOffering();

            TestAuthentication.asStudent(student1);

            assertThrows(AuthorizationDeniedException.class, () ->
                enrollmentService.createEnrollment(student2.getId(), offering.getId())
            );
        }

    }

    @Nested
    class ActionAuthorization {
        record ActionScenario(
            TestActor actor,
            EnrollmentAction action,
            Outcome outcome
        ) {}

        static Stream<ActionScenario> actions() {
            return Stream.of(
                new ActionScenario(TestActor.ADMIN, EnrollmentAction.APPROVE, Outcome.ALLOWED),
                new ActionScenario(TestActor.ADMIN, EnrollmentAction.REJECT, Outcome.ALLOWED),
                new ActionScenario(TestActor.ADMIN, EnrollmentAction.DROP, Outcome.NOT_ALLOWED),
                new ActionScenario(TestActor.ADMIN, EnrollmentAction.CANCEL, Outcome.NOT_ALLOWED),

                new ActionScenario(TestActor.OFFERING_INSTRUCTOR, EnrollmentAction.APPROVE, Outcome.ALLOWED),
                new ActionScenario(TestActor.OFFERING_INSTRUCTOR, EnrollmentAction.REJECT, Outcome.ALLOWED),
                new ActionScenario(TestActor.OFFERING_INSTRUCTOR, EnrollmentAction.CANCEL, Outcome.NOT_ALLOWED),
                new ActionScenario(TestActor.OFFERING_INSTRUCTOR, EnrollmentAction.DROP, Outcome.NOT_ALLOWED),

                new ActionScenario(TestActor.OTHER_INSTRUCTOR, EnrollmentAction.APPROVE, Outcome.ACCESS_DENIED),
                new ActionScenario(TestActor.OTHER_INSTRUCTOR, EnrollmentAction.REJECT, Outcome.ACCESS_DENIED),
                new ActionScenario(TestActor.OTHER_INSTRUCTOR, EnrollmentAction.CANCEL, Outcome.ACCESS_DENIED),
                new ActionScenario(TestActor.OTHER_INSTRUCTOR, EnrollmentAction.DROP, Outcome.ACCESS_DENIED),

                new ActionScenario(TestActor.OWNING_STUDENT, EnrollmentAction.APPROVE, Outcome.NOT_ALLOWED),
                new ActionScenario(TestActor.OWNING_STUDENT, EnrollmentAction.REJECT, Outcome.NOT_ALLOWED),
                new ActionScenario(TestActor.OWNING_STUDENT, EnrollmentAction.CANCEL, Outcome.ALLOWED),
                new ActionScenario(TestActor.OWNING_STUDENT, EnrollmentAction.DROP, Outcome.ALLOWED),

                new ActionScenario(TestActor.OTHER_STUDENT, EnrollmentAction.APPROVE, Outcome.ACCESS_DENIED),
                new ActionScenario(TestActor.OTHER_STUDENT, EnrollmentAction.REJECT, Outcome.ACCESS_DENIED),
                new ActionScenario(TestActor.OTHER_STUDENT, EnrollmentAction.CANCEL, Outcome.ACCESS_DENIED),
                new ActionScenario(TestActor.OTHER_STUDENT, EnrollmentAction.DROP, Outcome.ACCESS_DENIED)
            );
        }

        private EnrollmentStatus getValidStartingStatus(EnrollmentAction action) {
            return switch (action) {
                case APPROVE, REJECT ,CANCEL -> EnrollmentStatus.PENDING;
                case DROP ->  EnrollmentStatus.ENROLLED;
            };
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("actions")
        void shouldEnforceActionAuthorization(ActionScenario scenario) {
            var target = enrollmentScenarioSeeder
                .builder()
                .enrollmentStatus(getValidStartingStatus(scenario.action()))
                .student(s -> s.studentNumber(1))
                .offering(o -> o.capacity(10).enrolled(5).courseNumber(1).instructorNumber(1).semester(s -> s.registrationOpenOn(LocalDate.now(clock))))
                .build();

            var other = enrollmentScenarioSeeder
                .builder()
                .student(s -> s.studentNumber(2))
                .offering(o -> o.courseNumber(2).instructorNumber(2))
                .build();

            authenticateAs(scenario.actor(), target, other);

            switch (scenario.outcome) {
                case ALLOWED -> {
                    var resp = enrollmentService.performAction(target.enrollment().getId(), scenario.action());
                    assertNotNull(resp);
                    assertEquals(scenario.action.getTargetStatus(), resp.enrollmentStatus());
                }
                case NOT_ALLOWED -> {
                    assertThrows(ForbiddenException.class, () -> enrollmentService.performAction(target.enrollment().getId(), scenario.action()));
                }
                case ACCESS_DENIED -> {
                    assertThrows(AuthorizationDeniedException.class, () -> enrollmentService.performAction(target.enrollment().getId(), scenario.action()));
                }
            }

        }
    }

    @Nested
    class ReadAuthorization {

        record ReadScenario(
            TestActor actor,
            Outcome outcome
        ) {}

        static Stream<ReadScenario> readScenarios() {
            return Stream.of(
                new ReadScenario(TestActor.ADMIN, Outcome.ALLOWED),
                new ReadScenario(TestActor.OWNING_STUDENT, Outcome.ALLOWED),
                new ReadScenario(TestActor.OFFERING_INSTRUCTOR, Outcome.ALLOWED),
                new ReadScenario(TestActor.OTHER_INSTRUCTOR, Outcome.ACCESS_DENIED),
                new ReadScenario(TestActor.OTHER_STUDENT, Outcome.ACCESS_DENIED)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("readScenarios")
        void shouldEnforceReadAuthorization(ReadScenario scenario) {

            var target = enrollmentScenarioSeeder
                .builder()
                .student(s -> s.studentNumber(1))
                .offering(o -> o.capacity(10).enrolled(5).courseNumber(1).instructorNumber(1).semester(s -> s.registrationOpenOn(LocalDate.now(clock))))
                .build();

            var other = enrollmentScenarioSeeder
                .builder()
                .student(s -> s.studentNumber(2))
                .offering(o -> o.courseNumber(2).instructorNumber(2))
                .build();

            authenticateAs(scenario.actor(), target, other);

            switch (scenario.outcome) {
                case ALLOWED -> {
                    var resp = enrollmentService.getEnrollment(target.enrollment().getId());
                    assertNotNull(resp);
                    assertEquals(target.enrollment().getId(), resp.id());
                }
                case ACCESS_DENIED -> {
                    assertThrows(AuthorizationDeniedException.class, () -> enrollmentService.getEnrollment(target.enrollment().getId()));
                }
            }
        }

    }

    @Nested
    class Query {
        private StudentEntity student1, student2;

        @BeforeEach
        void setup() {
            var studentScenario1 = studentScenarioSeeder.builder()
                .department(d -> d.departmentNumber(1))
                .studentNumber(1)
                .build();

            var studentScenario2 = studentScenarioSeeder.builder()
                .department(d -> d.departmentNumber(1))
                .studentNumber(2)
                .build();

            var courseOfferingScenario = courseOfferingScenarioSeeder.builder()
                .departmentNumber(1)
                .courseNumber(1)
                .instructorNumber(1)
                .capacity(10)
                .enrolled(0)
                .build();

            student1 = studentScenario1.student();
            student2 = studentScenario2.student();

            TestAuthentication.asStudent(student1);
            enrollmentService.createEnrollment(student1.getId(), courseOfferingScenario.courseOffering().getId());
        }

        @Test
        void shouldAllowStudentOfEnrollment() {
            TestAuthentication.asStudent(student1);
            var resp = enrollmentService.getEnrollmentsForStudent(student1.getId(), null, null);
            assertNotNull(resp);
        }

        @Test
        void shouldDenyStudentNotOfEnrollment() {
            TestAuthentication.asStudent(student2);
            assertThrows(AuthorizationDeniedException.class, () -> enrollmentService.getEnrollmentsForStudent(student1.getId(), null, null));
        }

    }
}
