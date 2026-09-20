package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.time.Clock;
import java.time.LocalDate;

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

    @Nested
    class CreationAuthorization {
        private StudentScenario studentScenario1, studentScenario2;
        private CourseOfferingScenarioSeeder.Scenario courseOfferingScenarioBuilder;
        private UserEntity admin;

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

            TestAuthentication.clear();
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
    class EnrollmentTransition {
        private EnrollmentScenario enrollmentScenario1,  enrollmentScenario2;

        private enum AdminAction {
            approve,
            reject,
        }
        private enum InstructorAction  {
            approve,
            reject
        }
        private enum StudentAction {
            cancel,
            drop
        }

        @BeforeEach
        void setup() {
            enrollmentScenario1 = enrollmentScenarioSeeder.builder()
                .departmentNumber(1)
                .offering(
                    o -> o.instructorNumber(1).courseNumber(1)
                ).build();

            enrollmentScenario2 = enrollmentScenarioSeeder.builder()
                .student(s -> s.studentNumber(2))
                .departmentNumber(1)
                .offering(
                    o -> o.instructorNumber(2).courseNumber(2)
                ).build();
        }

        @ParameterizedTest
        @WithAdmin
        @EnumSource(AdminAction.class)
        void shouldAllowAdmin(AdminAction action) {
            var enrollment = enrollmentScenario1.enrollment();

            switch (action) {
                case approve: {
                    enrollmentService.approveEnrollment(enrollment.getId());
                    break;
                }
                case reject: {
                    enrollmentService.rejectEnrollment(enrollment.getId());
                    break;
                }
            }

            TestAuthentication.clear();
        }

        @ParameterizedTest
        @EnumSource(InstructorAction.class)
        void shouldAllowInstructorOfSameEnrollmentOffering(InstructorAction action) {
            var instructor = enrollmentScenario1.instructor();
            var enrollment = enrollmentScenario1.enrollment();

            TestAuthentication.asInstructor(instructor);

            switch (action) {
                case approve: {
                    enrollmentService.approveEnrollment(enrollment.getId());
                    break;
                }
                case reject: {
                    enrollmentService.rejectEnrollment(enrollment.getId());
                    break;
                }
            }

            TestAuthentication.clear();
        }

        @ParameterizedTest
        @EnumSource(InstructorAction.class)
        void shouldDenyDifferentInstructor(InstructorAction action) {
            var instructor = enrollmentScenario2.instructor();
            var enrollment = enrollmentScenario1.enrollment();

            TestAuthentication.asInstructor(instructor);

            switch (action) {
                case approve: {
                    assertThrows(AuthorizationDeniedException.class, () ->
                        enrollmentService.approveEnrollment(enrollment.getId())
                    );
                    break;
                }
                case reject: {
                    assertThrows(AuthorizationDeniedException.class, () ->
                        enrollmentService.rejectEnrollment(enrollment.getId())
                    );
                    break;
                }
            }

            TestAuthentication.clear();
        }

        @ParameterizedTest
        @EnumSource(StudentAction.class)
        void shouldAllowStudentOfEnrollment(StudentAction action) {
            var student = enrollmentScenario1.student();
            var enrollment = enrollmentScenario1.enrollment();

            TestAuthentication.asStudent(student);

            switch (action) {
                case drop: {
                    enrollmentService.dropEnrollment(enrollment.getId());
                    break;
                }
                case cancel: {
                    enrollmentService.cancelEnrollment(enrollment.getId());
                    break;
                }
            }

            TestAuthentication.clear();
        }

        @ParameterizedTest
        @EnumSource(StudentAction.class)
        void shouldDenyAnotherStudent(StudentAction action) {
            var student = enrollmentScenario2.student();
            var enrollment = enrollmentScenario1.enrollment();

            TestAuthentication.asStudent(student);

            switch (action) {
                case drop: {
                    assertThrows(AuthorizationDeniedException.class, () ->
                        enrollmentService.dropEnrollment(enrollment.getId())
                    );
                    break;
                }
                case cancel: {
                    assertThrows(AuthorizationDeniedException.class, () ->
                        enrollmentService.cancelEnrollment(enrollment.getId())
                    );
                    break;
                }
            }

            TestAuthentication.clear();
        }

    }
}
