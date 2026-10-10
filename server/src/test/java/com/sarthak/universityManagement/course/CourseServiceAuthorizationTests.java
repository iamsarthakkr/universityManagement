package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;

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

    enum TestActor { ADMIN, INSTRUCTOR, STUDENT, ANONYMOUS }
    enum Outcome { ALLOWED, ACCESS_DENIED, UNAUTHENTICATED }

    private void authenticateAs(TestActor actor) {
        switch (actor) {
            case ADMIN -> TestAuthentication.asRole(Role.ADMIN);
            case INSTRUCTOR -> TestAuthentication.asRole(Role.INSTRUCTOR);
            case STUDENT -> TestAuthentication.asRole(Role.STUDENT);
            case ANONYMOUS -> TestAuthentication.clear();
        }
    }

    private static Class<? extends Exception> expectedException(Outcome outcome) {
        return switch (outcome) {
            case ACCESS_DENIED -> AuthorizationDeniedException.class;
            case UNAUTHENTICATED -> AuthenticationCredentialsNotFoundException.class;
            case ALLOWED -> throw new IllegalArgumentException("ALLOWED has no exception");
        };
    }

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    @Nested
    class CreationAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(TestActor.ADMIN, Outcome.ALLOWED),
                Arguments.of(TestActor.INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.of(TestActor.STUDENT, Outcome.ACCESS_DENIED),
                Arguments.of(TestActor.ANONYMOUS, Outcome.UNAUTHENTICATED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldAuthorizeCreation(TestActor actor, Outcome outcome) {
            var department = departmentSeeder.saveDefault("cs-1");
            var request = CourseFixtures.courseRequest(department.getId()).build();
            authenticateAs(actor);

            if(outcome == Outcome.ALLOWED) {
                assertNotNull(courseService.createCourse(request).id());
            } else {
                assertThrows(expectedException(outcome), () -> courseService.createCourse(request));
                assertEquals(0, courseRepo.count());
            }
        }
    }

    @Nested
    class ReadAuthorization {

        // The course list and course details carry no @PreAuthorize: route security decides who reaches them
        @ParameterizedTest
        @EnumSource(TestActor.class)
        void shouldAllowEveryoneToReadCourses(TestActor actor) {
            var course = courseSeeder.saveDefault(departmentSeeder.saveDefault("cs-1"));
            authenticateAs(actor);

            assertEquals(course.getId(), courseService.getCourseById(course.getId()).id());
            assertEquals(1, courseService.getCourses().size());
        }
    }
}
