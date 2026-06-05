package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
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
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingRequests_whenAdmin_shouldAllow() {
        List<StudentRegistrationResponse> response =
            studentRegistrationService.getRequests(RegistrationStatus.PENDING);
        assertNotNull(response);
    }
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void getPendingRequests_whenStudent_shouldDeny() {
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
    @WithMockUser(roles = "ADMIN")
    void approveRegistration_whenAdmin_shouldAllow() {
        StudentRegistrationEntity saved = setup.savedStudentRegistration("student1", "student@abc");
        
        StudentRegistrationResponse response =
            studentRegistrationService.approveRegistration(saved.getId());
        assertEquals(RegistrationStatus.APPROVED, response.status());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void RejectRegistration_whenAdmin_shouldAllow() {
        StudentRegistrationEntity saved = setup.savedStudentRegistration("student1", "student@abc");
        
        StudentRegistrationResponse response =
            studentRegistrationService.rejectRegistration(saved.getId());
        assertEquals(RegistrationStatus.REJECTED, response.status());
    }
}
