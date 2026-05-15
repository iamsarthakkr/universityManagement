package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StudentRegistrationServiceIntegrationTests {
    
    private final StudentRegistrationService service;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final UserRepo userRepo;
    
    @Autowired
    public StudentRegistrationServiceIntegrationTests(
        StudentRegistrationService service,
        StudentRegistrationRepo studentRegistrationRepo,
        InstructorRegistrationRepo instructorRegistrationRepo,
        UserRepo userRepo
    ) {
        this.service = service;
        this.studentRegistrationRepo = studentRegistrationRepo;
        this.instructorRegistrationRepo = instructorRegistrationRepo;
        this.userRepo = userRepo;
    }
    
    
    private StudentRegistrationRequest validStudentRequest(String username, String email) {
        return new StudentRegistrationRequest(
            username, "secret", "John", "Doe", email,
            "1234567890", LocalDate.of(2000, 1, 1),
            "123 Main St", "Father", "Mother"
        );
    }
    
    private InstructorRegistrationEntity validInstructorEntity(String username, String email) {
        return InstructorRegistrationEntity.builder()
            .username(username)
            .email(email)
            .password("secret")
            .firstName("Jane")
            .lastName("Doe")
            .phoneNumber("1234567890")
            .department("Computer Science")
            .registrationStatus(RegistrationStatus.PENDING)
            .build();
    }
    
    @Test
    void shouldCreateStudentRegistrationSuccessfully() {
        var req = validStudentRequest("student1", "student1@example.com");
        StudentRegistrationResponse resp = service.createRegistration(req);
        
        assertNotNull(resp);
        assertEquals("student1", resp.username());
        assertEquals("student1@example.com", resp.email());
        
        var entity = studentRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
    }
    
    @Test
    void shouldCreateStudentRegistrationWithPendingStatus() {
        var req = validStudentRequest("student2", "student2@example.com");
        StudentRegistrationResponse resp = service.createRegistration(req);
        
        var entity = studentRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals(RegistrationStatus.PENDING, entity.getRegistrationStatus());
    }
    
    @Test
    void shouldRejectDuplicateUsernameInUserTable() {
        UserEntity user = new UserEntity();
        user.setUsername("dupeuser");
        user.setEmail("other@example.com");
        user.setPassword("pw");
        user.setRole(Role.STUDENT);
        user.setActive(true);
        userRepo.saveAndFlush(user);
        
        var req = validStudentRequest("dupeuser", "unique@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateEmailInUserTable() {
        UserEntity user = new UserEntity();
        user.setUsername("uniqueuser");
        user.setEmail("dupe@email.com");
        user.setPassword("pw");
        user.setRole(Role.STUDENT);
        user.setActive(true);
        userRepo.saveAndFlush(user);
        
        var req = validStudentRequest("anotheruser", "dupe@email.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateUsernameInStudentRegistrationTable() {
        var req1 = validStudentRequest("student3", "student3a@example.com");
        service.createRegistration(req1);
        
        var req2 = validStudentRequest("student3", "student3b@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }
    
    @Test
    void shouldRejectDuplicateEmailInStudentRegistrationTable() {
        var req1 = validStudentRequest("student4a", "student4@example.com");
        service.createRegistration(req1);
        
        var req2 = validStudentRequest("student4b", "student4@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }
    
    @Test
    void shouldRejectDuplicateUsernameInInstructorRegistrationTable() {
         instructorRegistrationRepo.saveAndFlush(validInstructorEntity("instructor1", "unique@email.com"));
         var req = validStudentRequest("instructor1", "student5@example.com");
         assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateEmailInInstructorRegistrationTable() {
         instructorRegistrationRepo.saveAndFlush(validInstructorEntity("uniqueuser", "dupe@email.com"));
         var req = validStudentRequest("student6", "dupe@email.com");
         assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldPersistStudentRegistration() {
        var req = validStudentRequest("student7", "student7@example.com");
        StudentRegistrationResponse resp = service.createRegistration(req);
        
        var entity = studentRegistrationRepo.findById(resp.id());
        assertTrue(entity.isPresent());
        assertEquals("student7", entity.get().getUsername());
    }
    
}
