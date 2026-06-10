// java
package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
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
public class InstructorRegistrationServiceAuthorizationTests {
    @Autowired
    private InstructorRegistrationService instructorRegistrationService;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
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
    
    private void setupInstructor() {
        var user = setup.savedUser("instructor1", "instructor1@gmail.com", Role.INSTRUCTOR);
        TestSecurityUtils.authenticateAs(user);
    }
    
    @Test
    void getPendingRequests_whenAdmin_shouldAllow() {
        setupAdmin();
        List<InstructorRegistrationResponse> response =
            instructorRegistrationService.getRequests(RegistrationStatus.PENDING);
        assertNotNull(response);
    }
    
    @Test
    void getPendingRequests_whenInstructor_shouldDeny() {
        setupInstructor();
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
        setupAdmin();
        InstructorRegistrationEntity saved = setup.savedInstructorRegistration("instr1", "instr@abc");
        
        InstructorRegistrationResponse response =
            instructorRegistrationService.approveRegistration(saved.getId());
        assertEquals(RegistrationStatus.APPROVED, response.status());
    }
    
    @Test
    void rejectRegistration_whenAdmin_shouldAllow() {
        setupAdmin();
        InstructorRegistrationEntity saved = setup.savedInstructorRegistration("instr2", "instr2@abc");
        
        InstructorRegistrationResponse response =
            instructorRegistrationService.rejectRegistration(saved.getId());
        assertEquals(RegistrationStatus.REJECTED, response.status());
    }
    
    @Test
    void approveRegistration_whenInstructor_shouldDeny() {
        setupInstructor();
        InstructorRegistrationEntity saved = setup.savedInstructorRegistration("instr1", "instr@abc");
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> instructorRegistrationService.approveRegistration(saved.getId())
        );
    }
    
    @Test
    void rejectRegistration_whenInstructor_shouldDeny() {
        setupInstructor();
        InstructorRegistrationEntity saved = setup.savedInstructorRegistration("instr2", "instr2@abc");
        
        assertThrows(
            AuthorizationDeniedException.class,
            () -> instructorRegistrationService.rejectRegistration(saved.getId())
        );
    }
}