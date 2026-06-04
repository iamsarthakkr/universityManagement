// java
package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
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
public class InstructorRegistrationServiceAuthorizationTests {
    @Autowired
    private InstructorRegistrationService instructorRegistrationService;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private TestDataSetup setup;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingRequests_whenAdmin_shouldAllow() {
        List<InstructorRegistrationResponse> response =
            instructorRegistrationService.getPendingRequests();
        assertNotNull(response);
    }
    
    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void getPendingRequests_whenInstructor_shouldDeny() {
        assertThrows(
            AuthorizationDeniedException.class,
            () -> instructorRegistrationService.getPendingRequests()
        );
    }
    
    @Test
    void getPendingRequests_whenAnonymous_shouldDeny() {
        assertThrows(
            AuthenticationCredentialsNotFoundException.class,
            () -> instructorRegistrationService.getPendingRequests()
        );
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRegistration_whenAdmin_shouldAllow() {
        InstructorRegistrationEntity saved = setup.savedInstructorRegistration("instr1", "instr@abc");
        
        InstructorRegistrationResponse response =
            instructorRegistrationService.approveRegistration(saved.getId());
        assertEquals(RegistrationStatus.APPROVED, response.status());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRegistration_whenAdmin_shouldAllow() {
        InstructorRegistrationEntity saved = setup.savedInstructorRegistration("instr2", "instr2@abc");
        
        InstructorRegistrationResponse response =
            instructorRegistrationService.rejectRegistration(saved.getId());
        assertEquals(RegistrationStatus.REJECTED, response.status());
    }
}