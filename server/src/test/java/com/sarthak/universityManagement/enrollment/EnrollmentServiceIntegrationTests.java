package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;

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
            assertEquals(student.getId(), resp.studentId());
            assertEquals(offering.getId(), resp.courseOfferingId());

            var updatedOffering = courseOfferingService.getCourseOfferingEntity(offering.getId());
            assertEquals(1, updatedOffering.getEnrolled());
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
            assertEquals(student.getId(), resp.studentId());
            assertEquals(offering.getId(), resp.courseOfferingId());

        }

    }

    @Nested
    class Transition {
        private EnrollmentScenarioSeeder.Scenario enrollmentScenarioBuilder;

        @BeforeEach
        void setUp() {
            enrollmentScenarioBuilder = enrollmentScenarioSeeder.builder();
        }

        @Test
        @WithAdmin
        void shouldApproveEnrollmentWhenSeatsAvailable() {
            var enrollmentScenario = enrollmentScenarioBuilder
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            var enrollment = enrollmentScenario.enrollment();

            enrollmentService.approveEnrollment(enrollment.getId());

            var saved = enrollmentService.getEnrollment(enrollment.getId());
            assertEquals(EnrollmentStatus.ENROLLED, saved.enrollmentStatus());

            var updatedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());
            assertEquals(10, updatedOffering.getEnrolled());

        }

        @Test
        @WithAdmin
        void shouldNotApproveEnrollmentWhenOfferingFull() {
            var enrollmentScenario = enrollmentScenarioBuilder
                .offering(o -> o.capacity(10).enrolled(10))
                .build();

            var enrollment = enrollmentScenario.enrollment();
            var ex = assertThrows(BadRequestException.class, () -> enrollmentService.approveEnrollment(enrollment.getId()));
            var msg = ex.getMessage();
            assertTrue(msg.contains("Insufficient capacity for offering "));

            var savedEnrollment = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(10, savedOffering.getEnrolled());
            assertEquals(EnrollmentStatus.PENDING, savedEnrollment.enrollmentStatus());
        }

        @ParameterizedTest
        @EnumSource(
            value = EnrollmentStatus.class,
            names = {"ENROLLED", "REJECTED", "CANCELLED", "DROPPED"}
        )
        @WithAdmin
        void shouldNotApproveInvalidTransition(EnrollmentStatus enrollmentStatus) {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(enrollmentStatus)
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            var enrollment = enrollmentScenario.enrollment();
            var ex = assertThrows(BadRequestException.class, () -> enrollmentService.approveEnrollment(enrollment.getId()));
            var msg = ex.getMessage();
            assertTrue(msg.contains("Enrollment with id " + enrollment.getId() + " cannot be approved"));

            var savedEnrollment = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(9, savedOffering.getEnrolled());
            assertEquals(enrollmentStatus, savedEnrollment.enrollmentStatus());
        }

       @Test
       @WithAdmin
       void shouldRejectEnrollment() {
            var enrollmentScenario = enrollmentScenarioBuilder
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            var enrollment = enrollmentScenario.enrollment();

            enrollmentService.rejectEnrollment(enrollment.getId());

            var saved = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(EnrollmentStatus.REJECTED, saved.enrollmentStatus());
            assertEquals(9,  savedOffering.getEnrolled());
       }

        @ParameterizedTest
        @EnumSource(
            value = EnrollmentStatus.class,
            names = {"ENROLLED", "REJECTED", "CANCELLED", "DROPPED"}
        )
        @WithAdmin
        void shouldNotRejectInvalidTransition(EnrollmentStatus enrollmentStatus) {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(enrollmentStatus)
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            var enrollment = enrollmentScenario.enrollment();
            var ex = assertThrows(BadRequestException.class, () -> enrollmentService.rejectEnrollment(enrollment.getId()));
            var msg = ex.getMessage();
            assertTrue(msg.contains("Enrollment with id " + enrollment.getId() + " cannot be rejected"));

            var savedEnrollment = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(9, savedOffering.getEnrolled());
            assertEquals(enrollmentStatus, savedEnrollment.enrollmentStatus());
       }

        @Test
        void shouldCancelEnrollment() {
            var enrollmentScenario = enrollmentScenarioBuilder
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            TestAuthentication.asStudent(enrollmentScenario.student());

            var enrollment = enrollmentScenario.enrollment();

            enrollmentService.cancelEnrollment(enrollment.getId());

            var saved = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(EnrollmentStatus.CANCELLED, saved.enrollmentStatus());
            assertEquals(9,  savedOffering.getEnrolled());
        }

        @ParameterizedTest
        @EnumSource(
            value = EnrollmentStatus.class,
            names = {"ENROLLED", "REJECTED", "CANCELLED", "DROPPED"}
        )
        void shouldNotCancelInvalidTransition(EnrollmentStatus enrollmentStatus) {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(enrollmentStatus)
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            TestAuthentication.asStudent(enrollmentScenario.student());

            var enrollment = enrollmentScenario.enrollment();
            var ex = assertThrows(BadRequestException.class, () -> enrollmentService.cancelEnrollment(enrollment.getId()));
            var msg = ex.getMessage();
            assertTrue(msg.contains("Enrollment with id " + enrollment.getId() + " cannot be cancelled"));

            var savedEnrollment = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(9, savedOffering.getEnrolled());
            assertEquals(enrollmentStatus, savedEnrollment.enrollmentStatus());

            TestAuthentication.clear();
        }

        @Test
        void shouldDropEnrollment() {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(EnrollmentStatus.ENROLLED)
                .offering(o -> o.capacity(10).enrolled(5))
                .build();

            TestAuthentication.asStudent(enrollmentScenario.student());

            var enrollment = enrollmentScenario.enrollment();

            enrollmentService.dropEnrollment(enrollment.getId());

            var saved = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(EnrollmentStatus.DROPPED, saved.enrollmentStatus());
            assertEquals(4,  savedOffering.getEnrolled());

            TestAuthentication.clear();
        }

        @ParameterizedTest
        @EnumSource(
            value = EnrollmentStatus.class,
            names = {"PENDING", "REJECTED", "CANCELLED", "DROPPED"}
        )
        void shouldNotDropInvalidTransition(EnrollmentStatus enrollmentStatus) {
            var enrollmentScenario = enrollmentScenarioBuilder
                .enrollmentStatus(enrollmentStatus)
                .offering(o -> o.capacity(10).enrolled(9))
                .build();

            TestAuthentication.asStudent(enrollmentScenario.student());

            var enrollment = enrollmentScenario.enrollment();
            var ex = assertThrows(BadRequestException.class, () -> enrollmentService.dropEnrollment(enrollment.getId()));
            var msg = ex.getMessage();
            assertTrue(msg.contains("Enrollment with id " + enrollment.getId() + " cannot be dropped"));

            var savedEnrollment = enrollmentService.getEnrollment(enrollment.getId());
            var savedOffering = courseOfferingService.getCourseOfferingEntity(enrollmentScenario.courseOffering().getId());

            assertEquals(9, savedOffering.getEnrolled());
            assertEquals(enrollmentStatus, savedEnrollment.enrollmentStatus());
        }

    }

}
