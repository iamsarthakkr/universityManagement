package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.ForbiddenException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.testUtils.scenario.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenario.enrollment.EnrollmentScenario;
import com.sarthak.universityManagement.testUtils.scenario.enrollment.EnrollmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenario.student.StudentScenario;
import com.sarthak.universityManagement.testUtils.scenario.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.security.AuthorizationService;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentAuthorizationTest extends IntegrationTests {
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private CurrentUserService currentUserService;

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

    private EnrollmentScenario seedTarget() {
        return seedTarget(EnrollmentStatus.PENDING);
    }

    private EnrollmentScenario seedTarget(EnrollmentStatus status) {
        return enrollmentScenarioSeeder
            .builder()
            .enrollmentStatus(status)
            .student(s -> s.studentNumber(1))
            .offering(o -> o.capacity(10).enrolled(5).courseNumber(1).instructorNumber(1).semester(s -> s.registrationOpenOn(LocalDate.now(clock))))
            .build();
    }

    private EnrollmentScenario seedOther() {
        return enrollmentScenarioSeeder
            .builder()
            .student(s -> s.studentNumber(2))
            .offering(o -> o.courseNumber(2).instructorNumber(2))
            .build();
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
            // Start where the action is a valid transition, so authorization is the only thing that can fail
            var target = seedTarget(getValidStartingStatus(scenario.action()));
            var other = seedOther();

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
            var target = seedTarget();
            var other = seedOther();

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

        @Test
        void shouldDenyMissingEnrollmentLikeAnUnrelatedOne() {
            var target = seedTarget();
            TestAuthentication.asStudent(target.student());

            // Same exception as for someone else's enrollment, so callers can't probe which ids exist
            assertThrows(AuthorizationDeniedException.class, () -> enrollmentService.getEnrollment(Integer.MAX_VALUE));
        }

    }

    @Nested
    class CourseOfferingReadAuthorization {
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.arguments(TestActor.ADMIN, Outcome.ALLOWED),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, Outcome.ALLOWED),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.arguments(TestActor.OWNING_STUDENT, Outcome.ACCESS_DENIED),
                Arguments.arguments(TestActor.OTHER_STUDENT, Outcome.ACCESS_DENIED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldEnforceCourseOfferingReadAuthorization(TestActor actor, Outcome outcome) {
            var target = seedTarget();
            var other = seedOther();
            var offeringId = target.courseOffering().getId();

            authenticateAs(actor, target, other);

            switch (outcome) {
                case ALLOWED -> {
                    var resp = enrollmentService.getEnrollmentsForCourseOffering(offeringId, null);
                    assertEquals(List.of(target.enrollment().getId()), resp.stream().map(EnrollmentResponse::id).toList());
                }
                case ACCESS_DENIED -> assertThrows(AuthorizationDeniedException.class,
                    () -> enrollmentService.getEnrollmentsForCourseOffering(offeringId, null));
            }
        }
    }

    @Nested
    class StudentEnrollmentsReadAuthorization {
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.arguments(TestActor.OWNING_STUDENT, Outcome.ALLOWED),
                Arguments.arguments(TestActor.OTHER_STUDENT, Outcome.ACCESS_DENIED),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.arguments(TestActor.ADMIN, Outcome.ACCESS_DENIED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldEnforceStudentEnrollmentsReadAuthorization(TestActor actor, Outcome outcome) {
            var target = seedTarget();
            var other = seedOther();
            var studentId = target.student().getId();

            authenticateAs(actor, target, other);

            switch (outcome) {
                case ALLOWED -> {
                    var resp = enrollmentService.getEnrollmentsForStudent(studentId, null, null);
                    assertEquals(List.of(target.enrollment().getId()), resp.stream().map(EnrollmentResponse::id).toList());
                }
                case ACCESS_DENIED -> assertThrows(AuthorizationDeniedException.class,
                    () -> enrollmentService.getEnrollmentsForStudent(studentId, null, null));
            }
        }
    }

    @Nested
        // The @PreAuthorize repo queries and the policy's in-memory checks encode the same relationship twice;
        // this fails the build if they ever drift apart
    class AnnotationPolicyConsistency {
        @ParameterizedTest(name = "{0}")
        @EnumSource(TestActor.class)
        void shouldAgreeOnWhoIsInvolved(TestActor actor) {
            var target = seedTarget();
            var other = seedOther();
            var enrollmentId = target.enrollment().getId();

            authenticateAs(actor, target, other);
            var principal = currentUserService.getCurrentUserPrincipal();

            boolean annotationAllows = principal.getRole() == Role.ADMIN
                || authorizationService.isInstructorForEnrollment(enrollmentId)
                || authorizationService.isStudentForEnrollment(enrollmentId);
            boolean policyAllows = EnrollmentActionPolicy.getActor(target.enrollment(), principal)
                != EnrollmentActionPolicy.EnrollmentActor.NONE;

            assertEquals(annotationAllows, policyAllows);
        }
    }

}
