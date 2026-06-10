package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
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
@Import(TestDataSetup.class)
@Transactional
public class StudentRegistrationServiceAuthorizationTests {
    @Autowired
    private StudentRegistrationService studentRegistrationService;
    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private TestDataSetup setup;
    
    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }
    
    private void setupAdmin() {
        var user = setup.savedUser("admin", "admin@gmail.com", Role.ADMIN);
        TestSecurityUtils.authenticateAs(user);
    }
    
    private void setupStudent() {
        var user = setup.savedUser("student1", "student1@gmail.com", Role.STUDENT);
        TestSecurityUtils.authenticateAs(user);
    }
    
    @Test
    void getPendingRequests_whenAdmin_shouldAllow() {
        setupAdmin();
        List<StudentRegistrationResponse> response =
            studentRegistrationService.getRequests(RegistrationStatus.PENDING);
        assertNotNull(response);
    }
    
    @Test
    void getPendingRequests_whenStudent_shouldDeny() {
        setupStudent();
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
        setupAdmin();
        StudentRegistrationEntity saved = setup.savedStudentRegistration("student1", "student@abc");
        
        StudentRegistrationResponse response =
            studentRegistrationService.approveRegistration(saved.getId());
        assertEquals(RegistrationStatus.APPROVED, response.status());
    }
    
    @Test
    void RejectRegistration_whenAdmin_shouldAllow() {
        setupAdmin();
        StudentRegistrationEntity saved = setup.savedStudentRegistration("student1", "student@abc");
        
        StudentRegistrationResponse response =
            studentRegistrationService.rejectRegistration(saved.getId());
        assertEquals(RegistrationStatus.REJECTED, response.status());
    }
    
    @Test
    void approveRegistration_whenStudent_shouldDeny() {
        setupStudent();
        StudentRegistrationEntity saved = setup.savedStudentRegistration("student1", "student@abc");
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> studentRegistrationService.approveRegistration(saved.getId())
        );
    }
    
    @Test
    void rejectRegistration_whenStudent_shouldDeny() {
        setupStudent();
        StudentRegistrationEntity saved = setup.savedStudentRegistration("student1", "student@abc");
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> studentRegistrationService.rejectRegistration(saved.getId())
        );
        
    }
    
}
