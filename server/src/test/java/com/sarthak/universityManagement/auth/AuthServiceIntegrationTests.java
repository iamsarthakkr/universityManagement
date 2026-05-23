package com.sarthak.universityManagement.auth;

import com.sarthak.universityManagement.auth.dto.LoginRequest;
import com.sarthak.universityManagement.auth.dto.LoginResponse;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataSetup.class)
@Transactional
public class AuthServiceIntegrationTests {
    @Autowired
    private AuthService authService;
    @Autowired
    private TestDataSetup setup;
    
    @Test
    void shouldAuthenticateValidUser() {
        setup.savedUser(
            "student1",
            "student1@example.com",
            Role.STUDENT,
            "password"
        );
        LoginRequest request =
            new LoginRequest(
                "student1",
                "password"
            );
        LoginResponse response =
            authService.login(request);
        assertNotNull(response);
    }
    
    @Test
    void shouldRejectInvalidUsername() {
        LoginRequest request = new LoginRequest("noSuchUser", "anyPassword");
       assertThrows(AuthenticationException.class,
            () -> authService.login(request));
    }
    
    @Test
    void shouldRejectInvalidPassword() {
        setup.savedUser(
            "student2",
            "student2@example.com",
            Role.STUDENT,
            "correct-password"
        );
        LoginRequest request = new LoginRequest("student2", "wrong-password");
        assertThrows(BadCredentialsException.class,
            () -> authService.login(request));
    }
    
    @Test
    void shouldGenerateJwtToken() {
        setup.savedUser(
            "student3",
            "student3@example.com",
            Role.STUDENT,
            "password123"
        );
        LoginRequest request = new LoginRequest("student3", "password123");
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        String token = response.accessToken();
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals("Bearer", response.tokenHeader());
    }
    
    @Test
    void shouldReturnCorrectUserDetailsInResponse() {
        setup.savedUser(
            "student4",
            "student4@example.com",
            Role.STUDENT,
            "pwd"
        );
        LoginRequest request = new LoginRequest("student4", "pwd");
        LoginResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("Bearer", response.tokenHeader());
        assertNotNull(response.accessToken());
    }
    
}
