package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@ActiveProfiles("test")
class InstructorRegistrationEntityTests {
    
    @Autowired
    private InstructorRegistrationRepo repo;
    
    private InstructorRegistrationEntity createEntity(String username, String email) {
        return InstructorRegistrationEntity.builder()
            .registrationStatus(RegistrationStatus.PENDING)
            .username(username)
            .password("secret")
            .firstName("Jane")
            .lastName("Doe")
            .email(email)
            .phoneNumber("1234567890")
            .department("Physics")
            .build();
    }
    
    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }
    
    @Test
    void shouldSaveRegistrationSuccessfully() {
        var entity = createEntity("instructor1", "instructor1@example.com");
        var saved = repo.saveAndFlush(entity);
        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }
    
    @Test
    void shouldRejectDuplicateUsername() {
        var first = createEntity("instructor2", "instructor2a@example.com");
        repo.saveAndFlush(first);
        var duplicate = createEntity("instructor2", "instructor2b@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        var first = createEntity("instructor3a", "instructor3@example.com");
        repo.saveAndFlush(first);
        var duplicate = createEntity("instructor3b", "instructor3@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectNullUsername() {
        var entity = createEntity("temp", "temp@example.com");
        entity.setUsername(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullEmail() {
        var entity = createEntity("temp2", "temp2@example.com");
        entity.setEmail(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullPassword() {
        var entity = createEntity("temp3", "temp3@example.com");
        entity.setPassword(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullFirstName() {
        var entity = createEntity("temp4", "temp4@example.com");
        entity.setFirstName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullPhoneNumber() {
        var entity = createEntity("temp5", "temp5@example.com");
        entity.setPhoneNumber(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullDepartment() {
        var entity = createEntity("temp6", "temp6@example.com");
        entity.setDepartment(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullRegistrationStatus() {
        var entity = createEntity("temp7", "temp7@example.com");
        entity.setRegistrationStatus(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldPopulateCreatedAtBeforeNow() {
        var entity = createEntity("audit1", "audit1@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant createdInstant = saved.getCreatedAt();
        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        assertTrue(createdInstant.isBefore(Instant.now()));
    }
    
    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        var entity = createEntity("audit2", "audit2@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        assertTrue(updatedInstant.isBefore(Instant.now()));
    }
    
    
    @Test
    void shouldReturnTrueWhenUsernameExists() {
        var entity = createEntity("uniqueuser", "uniqueuser@example.com");
        repo.saveAndFlush(entity);
        
        assertTrue(repo.existsByUsername("uniqueuser"));
        assertFalse(repo.existsByUsername("notpresent"));
    }
    
    @Test
    void shouldReturnTrueWhenEmailExists() {
        var entity = createEntity("emailuser", "emailuser@example.com");
        repo.saveAndFlush(entity);
        
        assertTrue(repo.existsByEmail("emailuser@example.com"));
        assertFalse(repo.existsByEmail("absent@example.com"));
    }
    
}
