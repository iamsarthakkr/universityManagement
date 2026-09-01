package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.seeders.StudentRegistrationSeeder;
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
public class StudentRegistrationServiceAuthorizationTests {
    @Autowired
    private StudentRegistrationService studentRegistrationService;

    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private StudentRegistrationSeeder studentRegistrationSeeder;

    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }

    private void setupUser(Role role) {
        var user = userSeeder.saveDefaultUser(role);
        TestSecurityUtils.authenticateAs(user);
    }
    

    @Test
    void getPendingRequests_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        List<StudentRegistrationResponse> response =
            studentRegistrationService.getRequests(RegistrationStatus.PENDING);
        assertNotNull(response);
    }
    
    @Test
    void getPendingRequests_whenStudent_shouldDeny() {
        setupUser(Role.STUDENT);
        assertThrows(
            AuthorizationDeniedException.class,
            () -> studentRegistrationService.getRequests(RegistrationStatus.PENDING)
        );
    }
    
    @Test
    void getPendingRequests_whenAnonymous_shouldDeny() {
        assertThrows(
            AuthenticationCredentialsNotFoundException.class,
            () -> studentRegistrationService.getRequests(RegistrationStatus.PENDING)
        );
    }
    
    @Test
    void approveRegistration_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        StudentRegistrationEntity saved = studentRegistrationSeeder.saveDefaultStudentRegistration();
        
        StudentRegistrationResponse response =
            studentRegistrationService.approveRegistration(saved.getId());
        assertEquals(RegistrationStatus.APPROVED, response.status());
    }
    
    @Test
    void RejectRegistration_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        StudentRegistrationEntity saved = studentRegistrationSeeder.saveDefaultStudentRegistration();

        StudentRegistrationResponse response =
            studentRegistrationService.rejectRegistration(saved.getId());
        assertEquals(RegistrationStatus.REJECTED, response.status());
    }
    
    @Test
    void approveRegistration_whenStudent_shouldDeny() {
        setupUser(Role.STUDENT);
        StudentRegistrationEntity saved = studentRegistrationSeeder.saveDefaultStudentRegistration();

        assertThrows(
            AuthorizationDeniedException.class,
            () -> studentRegistrationService.approveRegistration(saved.getId())
        );
    }
    
    @Test
    void rejectRegistration_whenStudent_shouldDeny() {
        setupUser(Role.STUDENT);
        StudentRegistrationEntity saved = studentRegistrationSeeder.saveDefaultStudentRegistration();
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> studentRegistrationService.rejectRegistration(saved.getId())
        );
    }

}
