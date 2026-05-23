package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.testUtils.TestDataFactory;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestDataFactory.class)
@ActiveProfiles("test")
@Transactional
public class StudentRegistrationServiceIntegrationTests {
    
    @Autowired
    private StudentRegistrationService service;
    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestDataFactory dataFactory;
    
    @Test
    void shouldCreateStudentRegistrationSuccessfully() {
        var req = dataFactory.studentRegistrationRequest("student1", "student1@example.com");
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
        var req = dataFactory.studentRegistrationRequest("student2", "student2@example.com");
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
        
        var req = dataFactory.studentRegistrationRequest("dupeuser", "unique@example.com");
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
        
        var req = dataFactory.studentRegistrationRequest("anotheruser", "dupe@email.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateUsernameInStudentRegistrationTable() {
        var req1 = dataFactory.studentRegistrationRequest("student3", "student3a@example.com");
        service.createRegistration(req1);
        
        var req2 = dataFactory.studentRegistrationRequest("student3", "student3b@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }
    
    @Test
    void shouldRejectDuplicateEmailInStudentRegistrationTable() {
        var req1 = dataFactory.studentRegistrationRequest("student4a", "student4@example.com");
        service.createRegistration(req1);
        
        var req2 = dataFactory.studentRegistrationRequest("student4b", "student4@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }
    
    @Test
    void shouldRejectDuplicateUsernameInInstructorRegistrationTable() {
         instructorRegistrationRepo.saveAndFlush(dataFactory.instructorRegistrationEntity("instructor1", "unique@email.com"));
         var req = dataFactory.studentRegistrationRequest("instructor1", "student5@example.com");
         assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateEmailInInstructorRegistrationTable() {
         instructorRegistrationRepo.saveAndFlush(dataFactory.instructorRegistrationEntity("uniqueuser", "dupe@email.com"));
         var req = dataFactory.studentRegistrationRequest("student6", "dupe@email.com");
         assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldPersistStudentRegistration() {
        var req = dataFactory.studentRegistrationRequest("student7", "student7@example.com");
        StudentRegistrationResponse resp = service.createRegistration(req);
        
        var entity = studentRegistrationRepo.findById(resp.id());
        assertTrue(entity.isPresent());
        assertEquals("student7", entity.get().getUsername());
    }
    
}
