package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.*;

public class CourseServiceAuthorizationTests extends IntegrationTests {

    @Autowired
    private CourseService courseService;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;


    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }

    private void setupUser(Role role) {
        var user = userSeeder.saveUser(
                UserFixtures.user().username("seeded-user").email("seeded@abc").role(role).build()
        );
        TestSecurityUtils.authenticateAs(user);
    }

    @Test
    void createCourse_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        var department = departmentSeeder.saveDefault("cs-1");

        var req = CourseFixtures.courseRequest(department.getId()).build();

        assertDoesNotThrow(() -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenStudent_shouldDeny() {
        setupUser(Role.STUDENT);
        var department = departmentSeeder.saveDefault("cs-1");

        var req = CourseFixtures.courseRequest(department.getId()).build();

        assertThrows(AuthorizationDeniedException.class, () -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenInstructor_shouldDeny() {
        setupUser(Role.INSTRUCTOR);
        var department = departmentSeeder.saveDefault("cs-1");

        var req = CourseFixtures.courseRequest(department.getId()).build();

        assertThrows(AuthorizationDeniedException.class, () -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenAnonymous_shouldDeny() {
        var department = departmentSeeder.saveDefault("cs-1");
        var req = CourseFixtures.courseRequest(department.getId()).build();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> courseService.createCourse(req));
    }
}
