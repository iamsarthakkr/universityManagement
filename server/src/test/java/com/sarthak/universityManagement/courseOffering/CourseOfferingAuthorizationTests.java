package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingFilter;
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
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    @Autowired
    private UserSeeder userSeeder;

    private static final CourseOfferingFilter NO_FILTER = new CourseOfferingFilter(null, null, null);

    private static Set<Integer> ids(List<CourseOfferingResponse> offerings) {
        return offerings.stream().map(CourseOfferingResponse::id).collect(Collectors.toSet());
    }

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
    class ListAuthorization {

        // Every logged-in role gets the same list; "my offerings" lives under /me
        @ParameterizedTest
        @EnumSource(value = RoleActor.class, names = {"ADMIN", "INSTRUCTOR", "STUDENT"})
        void shouldReturnSameListForEveryRole(RoleActor actor) {
            var department = departmentSeeder.saveDefault("dep");
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var first = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            var second = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            actor.authenticate();

            assertEquals(Set.of(first.getId(), second.getId()), ids(courseOfferingService.getCourseOfferings(NO_FILTER)));
        }

        @Test
        void anonymousShouldBeRejected() {
            RoleActor.ANONYMOUS.authenticate();

            assertThrows(AuthenticationCredentialsNotFoundException.class, () -> courseOfferingService.getCourseOfferings(NO_FILTER));
        }
    }

    @Nested
    class MyOfferings {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(RoleActor.INSTRUCTOR, AuthOutcome.ALLOWED),
                Arguments.of(RoleActor.ADMIN, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldOnlyLetInstructorsListTheirOfferings(RoleActor actor, AuthOutcome outcome) {
            var department = departmentSeeder.saveDefault("dep");
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var own = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");

            if(outcome == AuthOutcome.ALLOWED) {
                TestAuthentication.asInstructor(own.getInstructor());
                assertEquals(Set.of(own.getId()), ids(courseOfferingService.getCourseOfferingsForInstructor(NO_FILTER)));
            } else {
                actor.authenticate();
                assertThrows(outcome.expectedException(), () -> courseOfferingService.getCourseOfferingsForInstructor(NO_FILTER));
            }
        }

        @Test
        void shouldReturnOnlyTheCurrentInstructorsOfferings() {
            var department = departmentSeeder.saveDefault("dep");
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            // Extra users push user ids out of step with instructor ids, so mixing the two id spaces fails here
            userSeeder.saveDefault(Role.ADMIN);
            userSeeder.saveDefault(Role.ADMIN);
            var own = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            var instructor = own.getInstructor();
            assertNotEquals(instructor.getId(), instructor.getUser().getId(), "precondition: ids must differ");

            TestAuthentication.asInstructor(instructor);

            assertEquals(Set.of(own.getId()), ids(courseOfferingService.getCourseOfferingsForInstructor(NO_FILTER)));
        }

        @Test
        void shouldReturnNothingForInstructorWithoutOfferings() {
            var department = departmentSeeder.saveDefault("dep");
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");

            TestAuthentication.asInstructor(instructorSeeder.saveDefaultInstructor(department));

            assertTrue(courseOfferingService.getCourseOfferingsForInstructor(NO_FILTER).isEmpty());
        }

        // The scope comes from the principal; an instructorId sent by the client must not widen it
        @Test
        void shouldIgnoreClientSuppliedInstructorId() {
            var department = departmentSeeder.saveDefault("dep");
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var own = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");
            var someoneElses = courseOfferingSeeder.saveDefault(courseSeeder.saveDefault(department), semester, "A");

            TestAuthentication.asInstructor(own.getInstructor());
            var filter = new CourseOfferingFilter(null, null, someoneElses.getInstructor().getId());

            assertEquals(Set.of(own.getId()), ids(courseOfferingService.getCourseOfferingsForInstructor(filter)));
        }
    }
}
