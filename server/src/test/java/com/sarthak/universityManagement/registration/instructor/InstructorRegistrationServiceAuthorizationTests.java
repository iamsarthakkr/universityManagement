// java
package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.InstructorRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RegistrationTestConfig.class)
@Transactional
public class InstructorRegistrationServiceAuthorizationTests {
    @Autowired
    private InstructorRegistrationService instructorRegistrationService;
    @Autowired
    private InstructorRegistrationSeeder instructorRegistrationSeeder;
    @Autowired
    private UserSeeder userSeeder;

    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }

    private void setupUser(Role role) {
        var user = userSeeder.saveUser(
                UserFixtures.user() .username("seeded-user") .email("seeded@abc.com") .role(role) .build()
        );
        TestSecurityUtils.authenticateAs(user);
    }

    @Test
    void getPendingRequests_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        List<InstructorRegistrationResponse> response =
            instructorRegistrationService.getRequests(RegistrationStatus.PENDING);
        assertNotNull(response);
    }
    
    @Test
    void getPendingRequests_whenInstructor_shouldDeny() {
        setupUser(Role.INSTRUCTOR);
        assertThrows(
            AuthorizationDeniedException.class,
            () -> instructorRegistrationService.getRequests(RegistrationStatus.PENDING)
        );
    }
    
    @Test
    void getPendingRequests_whenAnonymous_shouldDeny() {
        assertThrows(
            AuthenticationCredentialsNotFoundException.class,
            () -> instructorRegistrationService.getRequests(RegistrationStatus.PENDING)
        );
    }
    
    @Test
    void approveRegistration_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        InstructorRegistrationEntity saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");

        InstructorRegistrationResponse response =
            instructorRegistrationService.approveRegistration(saved.getId());
        assertEquals(RegistrationStatus.APPROVED, response.status());
    }
    
    @Test
    void rejectRegistration_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        InstructorRegistrationEntity saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");

        InstructorRegistrationResponse response =
            instructorRegistrationService.rejectRegistration(saved.getId());
        assertEquals(RegistrationStatus.REJECTED, response.status());
    }
    
    @Test
    void approveRegistration_whenInstructor_shouldDeny() {
        setupUser(Role.INSTRUCTOR);
        InstructorRegistrationEntity saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> instructorRegistrationService.approveRegistration(saved.getId())
        );
    }
    
    @Test
    void rejectRegistration_whenInstructor_shouldDeny() {
        setupUser(Role.INSTRUCTOR);
        InstructorRegistrationEntity saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> instructorRegistrationService.rejectRegistration(saved.getId())
        );
    }
}