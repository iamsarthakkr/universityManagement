package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataSetup.class)
@Transactional
public class InstructorRegistrationAdminServiceIntegrationTests {
    @Autowired
    private InstructorRegistrationService service;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private InstructorRepo instructorRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestDataSetup setup;
    
    @Test
    void shouldApprovePendingInstructorRegistration() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.approveRegistration(saved.getId());
        
        var updated = instructorRegistrationRepo.findById(saved.getId()).orElseThrow();
        assertEquals(RegistrationStatus.APPROVED, updated.getRegistrationStatus());
    }
    
    @Test
    void shouldCreateUserWhenRegistrationApproved() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.approveRegistration(saved.getId());
        
        assertTrue(userRepo.existsByUsername("instructor1"));
        assertTrue(userRepo.existsByEmail("instructor1@example.com"));
    }
    
    @Test
    void shouldCreateInstructorWhenRegistrationApproved() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.approveRegistration(saved.getId());
        
        var user = userRepo.findByUsername("instructor1").orElseThrow();
        assertTrue(instructorRepo.existsByUserId(user.getId()));
    }
    
    @Test
    void shouldMarkRegistrationAsRejected() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.rejectRegistration(saved.getId());
        
        var updated = instructorRegistrationRepo.findById(saved.getId()).orElseThrow();
        assertEquals(RegistrationStatus.REJECTED, updated.getRegistrationStatus());
    }
    
    @Test
    void shouldNotCreateUserWhenRegistrationRejected() {
        var registration = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.rejectRegistration(registration.getId());
        
        assertFalse(userRepo.existsByUsername("instructor1"));
        assertFalse(userRepo.existsByEmail("instructor1@example.com"));
        
        assertEquals(0, userRepo.count());
        assertEquals(0, instructorRepo.count());
    }
    
    @Test
    void shouldThrowWhenRegistrationNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.approveRegistration(Integer.MAX_VALUE));
        assertThrows(ResourceNotFoundException.class, () -> service.rejectRegistration(Integer.MAX_VALUE));
    }
    
    @Test
    void shouldRejectApprovingApprovedRegistration() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.approveRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.approveRegistration(saved.getId()));
    }
    
    @Test
    void shouldRejectApprovingRejectedRegistration() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.rejectRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.approveRegistration(saved.getId()));
    }
    
    @Test
    void shouldRejectRejectingApprovedRegistration() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.approveRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.rejectRegistration(saved.getId()));
    }
    
    @Test
    void shouldRejectRejectingRejectedRegistration() {
        var saved = setup.savedInstructorRegistration("instructor1", "instructor1@example.com");
        service.rejectRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.rejectRegistration(saved.getId()));
    }
}
