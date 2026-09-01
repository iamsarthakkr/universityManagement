package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.StudentRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RegistrationTestConfig.class)
@Transactional
public class StudentRegistrationAdminServiceIntegrationTests {
    @Autowired
    private StudentRegistrationService service;

    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private StudentRegistrationSeeder studentRegistrationSeeder;
    @Autowired
    private UserSeeder userSeeder;
    
    private final String adminUsername = "admin";
    private final String adminEmail = "admin@gmail.com";
    
    @BeforeEach
    public void setup() {
        var user = userSeeder.saveUser(UserFixtures.user().username(adminUsername).email(adminEmail).build());
        TestSecurityUtils.authenticateAs(user);
    }
    
    @AfterEach
    public void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }
    
    @Test
    void shouldMarkRegistrationAsApproved() {

        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        var updated = studentRegistrationRepo.findById(saved.getId()).orElseThrow();
        assertEquals(RegistrationStatus.APPROVED, updated.getRegistrationStatus());
    }
    
    @Test
    void shouldSetCorrectReviewerAndReviewedAtWhenApproved() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        var updated = studentRegistrationRepo.findById(saved.getId()).orElseThrow();
        assertEquals(adminUsername, updated.getReviewedBy().getUsername());
        assertEquals(adminEmail, updated.getReviewedBy().getEmail());
        assertTrue(updated.getReviewedAt().isBefore(Instant.now()));
    }
    
    @Test
    void shouldCreateUserWhenRegistrationApproved() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        assertTrue(userRepo.existsByUsername("student1"));
        assertTrue(userRepo.existsByEmail("student1@example.com"));
    }
    
    @Test
    void shouldCreateStudentWhenRegistrationApproved() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        var user = userRepo.findByUsername("student1").orElseThrow();
        
        assertTrue(studentRepo.existsByUserId(user.getId()));
    }
    
    @Test
    void shouldMarkRegistrationAsRejected() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.rejectRegistration(saved.getId());
        
        var updated = studentRegistrationRepo.findById(saved.getId()).orElseThrow();
        
        assertEquals(RegistrationStatus.REJECTED, updated.getRegistrationStatus());
    }
    
    @Test
    void shouldSetCorrectReviewerAndReviewedAtWhenRejected() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.rejectRegistration(saved.getId());
        
        var updated = studentRegistrationRepo.findById(saved.getId()).orElseThrow();
        assertEquals(adminUsername, updated.getReviewedBy().getUsername());
        assertEquals(adminEmail, updated.getReviewedBy().getEmail());
        assertTrue(updated.getReviewedAt().isBefore(Instant.now()));
    }
    
    @Test
    void shouldNotCreateUserOrStudentWhenRegistrationRejected() {
        var registration = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("student1@example.com").build()
        );
        service.rejectRegistration(registration.getId());
        
        assertFalse(userRepo.existsByUsername("student1"));
        assertFalse(userRepo.existsByEmail("student1@example.com"));
        
        assertEquals(1, userRepo.count());
        assertEquals(0, studentRepo.count());
    }
    
    
    @Test
    void shouldThrowWhenRegistrationNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.approveRegistration(Integer.MAX_VALUE));
        assertThrows(ResourceNotFoundException.class, () -> service.rejectRegistration(Integer.MAX_VALUE));
    }
    
    @Test
    void shouldRejectApprovingApprovedRegistration() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student2").email("student2@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.approveRegistration(saved.getId()));
    }
    
    @Test
    void shouldRejectApprovingRejectedRegistration() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student3").email("student3@example.com").build()
        );
        service.rejectRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.approveRegistration(saved.getId()));
    }
    
    @Test
    void shouldRejectRejectingApprovedRegistration() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student4").email("student4@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.rejectRegistration(saved.getId()));
    }
    
    @Test
    void shouldRejectRejectingRejectedRegistration() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student5").email("student5@example.com").build()
        );
        service.rejectRegistration(saved.getId());
        
        assertThrows(ConflictException.class, () -> service.rejectRegistration(saved.getId()));
    }
    
    @Test
    void shouldCopyRegistrationDetailsToStudentWhenApproved() {
        var saved = studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student6").email("student6@example.com").build()
        );
        service.approveRegistration(saved.getId());
        
        var user = userRepo.findByUsername("student6").orElseThrow();
        var student = studentRepo.findByUserId(user.getId()).orElseThrow();
        
        assertEquals(saved.getFirstName(), student.getFirstName());
        assertEquals(saved.getLastName(), student.getLastName());
    }
}
