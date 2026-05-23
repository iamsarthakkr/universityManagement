package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.testUtils.TestDataFactory;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataFactory.class)
@Transactional
public class InstructorRegistrationServiceIntegrationTests {
    
    @Autowired
    private InstructorRegistrationService service;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestDataFactory dataFactory;
    
    @Test
    void shouldCreateInstructorRegistrationSuccessfully() {
        var req = dataFactory.instructorRegistrationRequest("instructor1", "instructor1@example.com");
        InstructorRegistrationResponse resp = service.createRegistration(req);
        
        assertNotNull(resp);
        assertEquals("instructor1", resp.username());
        assertEquals("instructor1@example.com", resp.email());
        
        var entity = instructorRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals("Jane", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
    }
    
    @Test
    void shouldCreateInstructorRegistrationWithPendingStatus() {
        var req = dataFactory.instructorRegistrationRequest("instructor2", "instructor2@example.com");
        InstructorRegistrationResponse resp = service.createRegistration(req);
        
        var entity = instructorRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals(RegistrationStatus.PENDING, entity.getRegistrationStatus());
    }
    
    @Test
    void shouldRejectDuplicateUsernameInUserTable() {
        UserEntity user = new UserEntity();
        user.setUsername("dupeuser");
        user.setEmail("other@example.com");
        user.setPassword("pw");
        user.setRole(Role.INSTRUCTOR);
        user.setActive(true);
        userRepo.saveAndFlush(user);
        
        var req = dataFactory.instructorRegistrationRequest("dupeuser", "unique@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateEmailInUserTable() {
        UserEntity user = new UserEntity();
        user.setUsername("uniqueuser");
        user.setEmail("dupe@email.com");
        user.setPassword("pw");
        user.setRole(Role.INSTRUCTOR);
        user.setActive(true);
        userRepo.saveAndFlush(user);
        
        var req = dataFactory.instructorRegistrationRequest("anotheruser", "dupe@email.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateUsernameInInstructorRegistrationTable() {
        var req1 = dataFactory.instructorRegistrationRequest("instructor3", "instructor3a@example.com");
        service.createRegistration(req1);
        
        var req2 = dataFactory.instructorRegistrationRequest("instructor3", "instructor3b@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }
    
    @Test
    void shouldRejectDuplicateEmailInInstructorRegistrationTable() {
        var req1 = dataFactory.instructorRegistrationRequest("instructor4a", "instructor4@example.com");
        service.createRegistration(req1);
        
        var req2 = dataFactory.instructorRegistrationRequest("instructor4b", "instructor4@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }
    
    @Test
    void shouldRejectDuplicateUsernameInStudentRegistrationTable() {
        studentRegistrationRepo.saveAndFlush(dataFactory.studentRegistrationEntity("student1", "unique@email.com"));
        var req = dataFactory.instructorRegistrationRequest("student1", "instructor5@example.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldRejectDuplicateEmailInStudentRegistrationTable() {
        studentRegistrationRepo.saveAndFlush(dataFactory.studentRegistrationEntity("uniqueuser", "dupe@email.com"));
        var req = dataFactory.instructorRegistrationRequest("instructor6", "dupe@email.com");
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }
    
    @Test
    void shouldPersistInstructorRegistration() {
        var req = dataFactory.instructorRegistrationRequest("instructor7", "instructor7@example.com");
        InstructorRegistrationResponse resp = service.createRegistration(req);
        
        var entity = instructorRegistrationRepo.findById(resp.id());
        assertTrue(entity.isPresent());
        assertEquals("instructor7", entity.get().getUsername());
    }
    
}