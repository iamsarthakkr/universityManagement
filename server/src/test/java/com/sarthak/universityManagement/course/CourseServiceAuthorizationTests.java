package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.security.AuthOutcome;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CourseServiceAuthorizationTests extends IntegrationTests {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private CourseSeeder courseSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

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
            var department = departmentSeeder.saveDefault("cs-1");
            var request = CourseFixtures.courseRequest(department.getId()).build();
            actor.authenticate();

            if(outcome == AuthOutcome.ALLOWED) {
                assertNotNull(courseService.createCourse(request).id());
            } else {
                assertThrows(outcome.expectedException(), () -> courseService.createCourse(request));
                assertEquals(0, courseRepo.count());
            }
        }
    }

    @Nested
    class ReadAuthorization {

        // The course list and course details carry no @PreAuthorize: route security decides who reaches them
        @ParameterizedTest
        @EnumSource(RoleActor.class)
        void shouldAllowEveryoneToReadCourses(RoleActor actor) {
            var course = courseSeeder.saveDefault(departmentSeeder.saveDefault("cs-1"));
            actor.authenticate();

            assertEquals(course.getId(), courseService.getCourseById(course.getId()).id());
            assertEquals(1, courseService.getCourses().size());
        }
    }
}
