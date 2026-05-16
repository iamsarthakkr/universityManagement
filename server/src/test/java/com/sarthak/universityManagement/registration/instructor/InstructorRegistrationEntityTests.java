package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.config.JpaConfig;
import com.sarthak.universityManagement.testUtils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@ActiveProfiles("test")
@Transactional
class InstructorRegistrationEntityTests {
    
    @Autowired
    private InstructorRegistrationRepo repo;
    
    @Test
    void shouldSaveRegistrationSuccessfully() {
        var entity = TestDataFactory.instructorRegistrationEntity("instructor1", "instructor1@example.com");
        var saved = repo.saveAndFlush(entity);
        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }
    
    @Test
    void shouldRejectDuplicateUsername() {
        var first = TestDataFactory.instructorRegistrationEntity("instructor2", "instructor2a@example.com");
        repo.saveAndFlush(first);
        var duplicate = TestDataFactory.instructorRegistrationEntity("instructor2", "instructor2b@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        var first = TestDataFactory.instructorRegistrationEntity("instructor3a", "instructor3@example.com");
        repo.saveAndFlush(first);
        var duplicate = TestDataFactory.instructorRegistrationEntity("instructor3b", "instructor3@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectNullUsername() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp", "temp@example.com");
        entity.setUsername(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullEmail() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp2", "temp2@example.com");
        entity.setEmail(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullPassword() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp3", "temp3@example.com");
        entity.setPassword(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullFirstName() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp4", "temp4@example.com");
        entity.setFirstName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullPhoneNumber() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp5", "temp5@example.com");
        entity.setPhoneNumber(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullDepartment() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp6", "temp6@example.com");
        entity.setDepartment(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullRegistrationStatus() {
        var entity = TestDataFactory.instructorRegistrationEntity("temp7", "temp7@example.com");
        entity.setRegistrationStatus(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldPopulateCreatedAtBeforeNow() {
        var entity = TestDataFactory.instructorRegistrationEntity("audit1", "audit1@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant createdInstant = saved.getCreatedAt();
        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        assertTrue(createdInstant.isBefore(Instant.now()));
    }
    
    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        var entity = TestDataFactory.instructorRegistrationEntity("audit2", "audit2@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        assertTrue(updatedInstant.isBefore(Instant.now()));
    }
    
    
    @Test
    void shouldReturnTrueWhenUsernameExists() {
        var entity = TestDataFactory.instructorRegistrationEntity("uniqueuser", "uniqueuser@example.com");
        repo.saveAndFlush(entity);
        
        assertTrue(repo.existsByUsername("uniqueuser"));
        assertFalse(repo.existsByUsername("notpresent"));
    }
    
    @Test
    void shouldReturnTrueWhenEmailExists() {
        var entity = TestDataFactory.instructorRegistrationEntity("emailuser", "emailuser@example.com");
        repo.saveAndFlush(entity);
        
        assertTrue(repo.existsByEmail("emailuser@example.com"));
        assertFalse(repo.existsByEmail("absent@example.com"));
    }
    
}
