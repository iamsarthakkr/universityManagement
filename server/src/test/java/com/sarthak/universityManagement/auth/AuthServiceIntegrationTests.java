package com.sarthak.universityManagement.auth;

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

    // Login compares against the stored hash, so the password must go through the app's encoder
    private UserEntity saveUser(String username, String rawPassword) {
        return userSeeder.saveUser(
            UserFixtures.user()
                .username(username)
                .email(username + "@example.com")
                .role(Role.STUDENT)
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
}
