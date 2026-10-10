package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentServiceIntegrationTests extends IntegrationTests {
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private CourseOfferingService courseOfferingService;

    @Autowired
    private StudentScenarioSeeder studentScenarioSeeder;
    @Autowired
    private CourseOfferingScenarioSeeder courseOfferingScenarioSeeder;
    @Autowired
    private EnrollmentScenarioSeeder enrollmentScenarioSeeder;
    @Autowired
    private Clock clock;

    @Nested
    class Creation {
        private StudentScenario studentScenario;
        private CourseOfferingScenarioSeeder.Scenario courseOfferingScenarioBuilder;

        @BeforeEach
        void setup() {
            studentScenario = studentScenarioSeeder.builder()
                .department(d -> d.departmentNumber(1))
                .studentNumber(1)
                .build();

            courseOfferingScenarioBuilder = courseOfferingScenarioSeeder.builder()
                .departmentNumber(1)
                .courseNumber(1)
                .instructorNumber(1)
                .capacity(10)
                .enrolled(0)
                .semester(s -> s.registrationOpenOn(LocalDate.now(clock)));


            TestAuthentication.asStudent(studentScenario.student());
        }

        @AfterEach
        void tearDown() {
            TestAuthentication.clear();
        }

        @Test
        void shouldCreateEnrollmentForStudent() {

            var student = studentScenario.student();
            var offering = courseOfferingScenarioBuilder.build().courseOffering();

            var resp = enrollmentService.createEnrollment(
                student.getId(),
                offering.getId()
            );

            assertNotNull(resp);

            assertEquals(EnrollmentStatus.PENDING, resp.enrollmentStatus());
            assertEquals(student.getId(), resp.student().id());
            assertEquals(offering.getId(), resp.courseOffering().id());

            var updatedOffering = courseOfferingService.getCourseOfferingEntity(offering.getId());
            assertEquals(0, updatedOffering.getEnrolled());
        }

        @Test
        void shouldNotCreateEnrollmentWhenRegistrationClosed() {
            var student = studentScenario.student();
            var offering = courseOfferingScenarioBuilder
                .semester(s -> s.registrationClosedBefore(LocalDate.now(clock)))
                .build()
                .courseOffering();

            assertThrows(BadRequestException.class, () -> enrollmentService.createEnrollment(student.getId(), offering.getId()));

        }

        @Test
        void shouldNotCreateEnrollmentWhenStudentAlreadyEnrolledToOffering() {
            var student = studentScenario.student();
            var offering = courseOfferingScenarioBuilder
                .build()
                .courseOffering();

            enrollmentService.createEnrollment(student.getId(), offering.getId());

            assertThrows(BadRequestException.class, () -> enrollmentService.createEnrollment(student.getId(), offering.getId()));
        }

        @Test
        void shouldAllowPendingEnrollmentRequestWhenCourseFull() {
            var student = studentScenario.student();
            var offering = courseOfferingScenarioBuilder
                .capacity(10)
                .enrolled(10)
                .build()
                .courseOffering();

            var resp = enrollmentService.createEnrollment(student.getId(), offering.getId());

            assertNotNull(resp);
            assertEquals(EnrollmentStatus.PENDING, resp.enrollmentStatus());
            assertEquals(student.getId(), resp.student().id());
            assertEquals(offering.getId(), resp.courseOffering().id());

        }

    }

    @Nested
    class Transition {
        private EnrollmentScenarioSeeder.Scenario enrollmentScenarioBuilder;

        @BeforeEach
        void setUp() {
            enrollmentScenarioBuilder = enrollmentScenarioSeeder.builder();
        }

        @AfterEach
        void tearDown() {
            TestAuthentication.clear();
        }

        final int capacity = 10;
        record TransitionCase(
            EnrollmentAction action,
            EnrollmentStatus from,
            EnrollmentStatus to,
            Integer enrolledBefore,
            Integer enrolledAfter
        ) {}

        static Stream<TransitionCase> validTransitionCases() {
            return Stream.of(
                new TransitionCase(EnrollmentAction.APPROVE, EnrollmentStatus.PENDING, EnrollmentStatus.ENROLLED, 9, 10),
                new TransitionCase(EnrollmentAction.REJECT, EnrollmentStatus.PENDING, EnrollmentStatus.REJECTED, 9, 9),
                new TransitionCase(EnrollmentAction.CANCEL, EnrollmentStatus.PENDING, EnrollmentStatus.CANCELLED, 9, 9),
                new TransitionCase(EnrollmentAction.DROP, EnrollmentStatus.ENROLLED, EnrollmentStatus.DROPPED, 10, 9)
            );
        }

        static Stream<Arguments> invalidTransitionCases() {
            List<Arguments> invalidTransitionCases = new ArrayList<>();
            for(var valid: validTransitionCases().toList()) {
                for(var status: EnrollmentStatus.values()) {
                    if(status != valid.from()) {
                        invalidTransitionCases.add(Arguments.of(valid.action(), status));
                    }
                }
            }

            return invalidTransitionCases.stream();
        }

        private void authenticateFor(EnrollmentAction action, EnrollmentScenario scenario) {
            switch (action) {
                case APPROVE, REJECT: TestAuthentication.asInstructor(scenario.instructor()); break;
                case DROP, CANCEL: TestAuthentication.asStudent(scenario.student()); break;
            }
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("validTransitionCases")
        void shouldPerformValidTransition(TransitionCase transitionCase) {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(transitionCase.from())
                .offering(o -> o.capacity(capacity).enrolled(transitionCase.enrolledBefore()))
                .build();

            authenticateFor(transitionCase.action(), enrollmentScenario);

            var enrollment = enrollmentScenario.enrollment();

            var before = enrollmentService.getEnrollment(enrollment.getId());
            assertTrue(before.allowedActions().contains(transitionCase.action()));

            var response = enrollmentService.performAction(enrollment.getId(), transitionCase.action());
            // Every valid transition leaves the acting party with nothing further to do on this enrollment
            assertEquals(List.of(), response.allowedActions());

            var saved = enrollmentService.getEnrollment(enrollment.getId());
            assertEquals(transitionCase.to(), saved.enrollmentStatus());

            var updatedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());
            assertEquals(transitionCase.enrolledAfter(), updatedOffering.getEnrolled());
        }

        @ParameterizedTest(name = "{0} from {1}")
        @MethodSource("invalidTransitionCases")
        void shouldRejectInvalidTransition(EnrollmentAction action, EnrollmentStatus from) {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(from)
                .offering(o -> o.capacity(capacity).enrolled(5).semester(s -> s.registrationOpenOn(LocalDate.now(clock))))
                .build();

            authenticateFor(action, enrollmentScenario);

            var enrollment = enrollmentScenario.enrollment();

            var before = enrollmentService.getEnrollment(enrollment.getId());
            assertFalse(before.allowedActions().contains(action));

            assertThrows(BadRequestException.class, () -> enrollmentService.performAction(enrollment.getId(), action));

            var savedEnrollment = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(5, savedOffering.getEnrolled());
            assertEquals(from, savedEnrollment.enrollmentStatus());
        }

        @Test
        void shouldReflectViewerInAllowedActionsAfterApproval() {
            var enrollmentScenario = enrollmentScenarioBuilder
                .offering(o -> o.capacity(capacity).enrolled(5))
                .build();
            var enrollmentId = enrollmentScenario.enrollment().getId();

            TestAuthentication.asInstructor(enrollmentScenario.instructor());
            enrollmentService.performAction(enrollmentId, EnrollmentAction.APPROVE);

            TestAuthentication.asStudent(enrollmentScenario.student());
            var studentView = enrollmentService.getEnrollment(enrollmentId);

            assertEquals(EnrollmentStatus.ENROLLED, studentView.enrollmentStatus());
            assertEquals(List.of(EnrollmentAction.DROP), studentView.allowedActions());
        }

    }

    @Nested
    class ApprovalRules {
        private static final int CAPACITY = 10;

        @AfterEach
        void tearDown() {
            TestAuthentication.clear();
        }

        @Test
        void shouldNotApproveWhenOfferingFull() {
            var enrollmentScenario = enrollmentScenarioSeeder.builder()
                .offering(o -> o.capacity(CAPACITY).enrolled(CAPACITY))
                .build();
            var enrollmentId = enrollmentScenario.enrollment().getId();
            TestAuthentication.asInstructor(enrollmentScenario.instructor());

            assertThrows(ConflictException.class, () -> enrollmentService.performAction(enrollmentId, EnrollmentAction.APPROVE));

            var savedEnrollment = enrollmentService.getEnrollment(enrollmentId);
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());
            assertEquals(EnrollmentStatus.PENDING, savedEnrollment.enrollmentStatus());
            assertEquals(CAPACITY, savedOffering.getEnrolled());
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = SemesterStatus.class, names = {"COMPLETED", "CANCELLED"})
        void shouldNotApproveWhenSemesterClosed(SemesterStatus semesterStatus) {
            var enrollmentScenario = enrollmentScenarioSeeder.builder()
                .offering(o -> o.capacity(CAPACITY).enrolled(5).semester(s -> s.status(semesterStatus)))
                .build();
            var enrollmentId = enrollmentScenario.enrollment().getId();
            TestAuthentication.asInstructor(enrollmentScenario.instructor());

            assertThrows(BadRequestException.class, () -> enrollmentService.performAction(enrollmentId, EnrollmentAction.APPROVE));

            var savedEnrollment = enrollmentService.getEnrollment(enrollmentId);
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());
            assertEquals(EnrollmentStatus.PENDING, savedEnrollment.enrollmentStatus());
            assertEquals(5, savedOffering.getEnrolled());
        }
    }
}

