package com.sarthak.universityManagement.auth;

import com.sarthak.universityManagement.testUtils.fixtures.InstructorFixtures;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.fixtures.StudentFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentSeeder;
import com.sarthak.universityManagement.auth.dto.LoginRequest;
import com.sarthak.universityManagement.auth.dto.LoginResponse;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.user.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceIntegrationTests extends IntegrationTests {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StudentSeeder studentSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;

    // Login compares against the stored hash, so the password must go through the app's encoder
    // Admin by default: it has no profile row, so credential tests don't need to seed one
    private UserEntity saveUser(String username, String rawPassword) {
        return saveUser(username, rawPassword, Role.ADMIN);
    }

    private UserEntity saveUser(String username, String rawPassword, Role role) {
        return userSeeder.saveUser(
            UserFixtures.user()
                .username(username)
                .email(username + "@example.com")
                .role(role)
                .password(passwordEncoder.encode(rawPassword))
                .build()
        );
    }

    @Test
    void shouldAuthenticateValidUser() {
        saveUser("student1", "password");

        LoginResponse response = authService.login(new LoginRequest("student1", "password"));

        assertNotNull(response);
    }

    @Test
    void shouldRejectInvalidUsername() {
        LoginRequest request = new LoginRequest("noSuchUser", "anyPassword");
        // Same exception as a wrong password, so login can't reveal which usernames exist
        assertThrows(BadCredentialsException.class,
            () -> authService.login(request));
    }

    @Test
    void shouldRejectInvalidPassword() {
        saveUser("student2", "correct-password");

        LoginRequest request = new LoginRequest("student2", "wrong-password");
        assertThrows(BadCredentialsException.class,
            () -> authService.login(request));
    }

    @Test
    void shouldGenerateJwtToken() {
        saveUser("student3", "password123");

        LoginResponse response = authService.login(new LoginRequest("student3", "password123"));

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.user());
    }

    @Test
    void shouldReturnCorrectUserDetailsInResponse() {
        var user = saveUser("student4", "pwd");

        LoginResponse response = authService.login(new LoginRequest("student4", "pwd"));

        assertNotNull(response.accessToken());
        assertEquals(user.getId(), response.user().id());
        assertEquals(user.getRole(), response.user().role());
        assertEquals(user.getUsername(), response.user().username());
    }

    // Login runs before any security context exists, so the user must come from the authenticated principal
    @Test
    void shouldReturnStudentProfileOnLogin() {
        var department = departmentSeeder.saveDefault("dep1");
        var user = saveUser("student5", "pwd", Role.STUDENT);
        var student = studentSeeder.save(StudentFixtures.student(department).user(user).build());

        LoginResponse response = authService.login(new LoginRequest("student5", "pwd"));

        assertEquals(user.getEmail(), response.user().email());
        assertEquals(Role.STUDENT, response.user().role());
        assertEquals(student.getId(), response.user().profile().id());
        assertEquals(department.getId(), response.user().profile().department().id());
    }

    @Test
    void shouldReturnNoProfileForAdminOnLogin() {
        var user = saveUser("admin5", "pwd", Role.ADMIN);

        LoginResponse response = authService.login(new LoginRequest("admin5", "pwd"));

        assertEquals(user.getEmail(), response.user().email());
        assertEquals(Role.ADMIN, response.user().role());
        assertNull(response.user().profile());
    }

    @Test
    void shouldReturnInstructorProfileOnLogin() {
        var department = departmentSeeder.saveDefault("dep1");
        var user = saveUser("instructor5", "pwd", Role.INSTRUCTOR);
        var instructor = instructorSeeder.saveInstructor(InstructorFixtures.instructorWithDepartment(department).user(user).build());

        LoginResponse response = authService.login(new LoginRequest("instructor5", "pwd"));

        assertEquals(Role.INSTRUCTOR, response.user().role());
        assertEquals(instructor.getId(), response.user().profile().id());
        assertEquals(department.getId(), response.user().profile().department().id());
    }
}
