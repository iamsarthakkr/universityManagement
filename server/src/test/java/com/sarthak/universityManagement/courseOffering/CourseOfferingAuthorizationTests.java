package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import com.sarthak.universityManagement.testUtils.security.AuthOutcome;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.CourseOfferingSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CourseOfferingAuthorizationTests extends IntegrationTests {
    @Autowired
    private CourseOfferingService courseOfferingService;
    @Autowired
    private CourseOfferingRepo courseOfferingRepo;

    @Autowired
    private CourseSeeder courseSeeder;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private CourseOfferingSeeder courseOfferingSeeder;

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    @Nested
    class CreationAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(RoleActor.ADMIN, AuthOutcome.ALLOWED),
                Arguments.of(RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldAuthorizeCreation(RoleActor actor, AuthOutcome outcome) {
            var department = departmentSeeder.saveDefault("dep");
            var request = CourseOfferingFixtures.courseOfferingRequest()
                .courseId(courseSeeder.saveDefault(department).getId())
                .semesterId(semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026).getId())
                .instructorId(instructorSeeder.saveDefaultInstructor(department).getId())
                .build();
            actor.authenticate();

            if(outcome == AuthOutcome.ALLOWED) {
                assertNotNull(courseOfferingService.createOffering(request).id());
            } else {
                assertThrows(outcome.expectedException(), () -> courseOfferingService.createOffering(request));
                assertEquals(0, courseOfferingRepo.count());
            }
        }
    }

    @Nested
    class ListVisibility {

        enum Viewer { ADMIN, OFFERING_INSTRUCTOR, OTHER_INSTRUCTOR, STUDENT }
        enum Visible { ALL, OWN, NONE }

        // Instructors only manage their own offerings; admins and students see the whole list
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(Viewer.ADMIN, Visible.ALL),
                Arguments.of(Viewer.STUDENT, Visible.ALL),
                Arguments.of(Viewer.OFFERING_INSTRUCTOR, Visible.OWN),
                Arguments.of(Viewer.OTHER_INSTRUCTOR, Visible.NONE)
            );
        }

        @ParameterizedTest(name = "{0} sees {1}")
        @MethodSource("cases")
        void shouldListOfferingsVisibleToViewer(Viewer viewer, Visible visible) {
            var department = departmentSeeder.saveDefault("dep");
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var own = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            var someoneElses = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            var otherInstructor = instructorSeeder.saveDefaultInstructor(department);

            switch (viewer) {
                case ADMIN -> TestAuthentication.asRole(Role.ADMIN);
                case STUDENT -> TestAuthentication.asRole(Role.STUDENT);
                case OFFERING_INSTRUCTOR -> TestAuthentication.asInstructor(own.getInstructor());
                case OTHER_INSTRUCTOR -> TestAuthentication.asInstructor(otherInstructor);
            }

            var expected = switch (visible) {
                case ALL -> List.of(own.getId(), someoneElses.getId());
                case OWN -> List.of(own.getId());
                case NONE -> List.<Integer>of();
            };

            var ids = courseOfferingService.getCourseOfferings(semester.getId())
                .stream()
                .map(CourseOfferingResponse::id)
                .sorted()
                .toList();

            assertEquals(expected.stream().sorted().toList(), ids);
        }

        @Test
        void anonymousShouldBeRejected() {
            RoleActor.ANONYMOUS.authenticate();

            assertThrows(AuthenticationCredentialsNotFoundException.class, () -> courseOfferingService.getCourseOfferings(null));
        }
    }
}
