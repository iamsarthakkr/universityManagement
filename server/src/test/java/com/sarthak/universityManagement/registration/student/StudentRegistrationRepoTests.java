package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.config.JpaConfig;
import com.sarthak.universityManagement.testUtils.TestDataFactory;
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
@Import({JpaConfig.class, TestDataFactory.class})
@ActiveProfiles("test")
@Transactional
class StudentRegistrationRepoTests {
    
    @Autowired
    private StudentRegistrationRepo repo;
    @Autowired
    private TestDataFactory dataFactory;
    
    @Test
    void shouldSaveRegistrationSuccessfully() {
        var entity = dataFactory.studentRegistrationEntity("student1", "student1@example.com");
        var saved = repo.saveAndFlush(entity);
        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }
    
    @Test
    void shouldRejectDuplicateUsername() {
        var first = dataFactory.studentRegistrationEntity("student2", "student2a@example.com");
        repo.saveAndFlush(first);
        var duplicate = dataFactory.studentRegistrationEntity("student2", "student2b@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        var first = dataFactory.studentRegistrationEntity("student3a", "student3@example.com");
        repo.saveAndFlush(first);
        var duplicate = dataFactory.studentRegistrationEntity("student3b", "student3@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectNullUsername() {
        var entity = dataFactory.studentRegistrationEntity("temp", "temp@example.com");
        entity.setUsername(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullEmail() {
        var entity = dataFactory.studentRegistrationEntity("temp2", "temp2@example.com");
        entity.setEmail(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullPassword() {
        var entity = dataFactory.studentRegistrationEntity("temp3", "temp3@example.com");
        entity.setPassword(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullFirstName() {
        var entity = dataFactory.studentRegistrationEntity("temp4", "temp4@example.com");
        entity.setFirstName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullDateOfBirth() {
        var entity = dataFactory.studentRegistrationEntity("temp6", "temp6@example.com");
        entity.setDateOfBirth(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullRegistrationStatus() {
        var entity = dataFactory.studentRegistrationEntity("temp10", "temp10@example.com");
        entity.setRegistrationStatus(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldPopulateCreatedAtBeforeNow() {
        var entity = dataFactory.studentRegistrationEntity("audit1", "audit1@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant createdInstant = saved.getCreatedAt();
        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        assertTrue(createdInstant.isBefore(Instant.now()));
    }
    
    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        var entity = dataFactory.studentRegistrationEntity("audit2", "audit2@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        assertTrue(updatedInstant.isBefore(Instant.now()));
    }
    
    @Test
    void shouldReturnTrueWhenUsernameExists() {
        var entity = dataFactory.studentRegistrationEntity("uniqueuser", "uniqueuser@example.com");
        repo.saveAndFlush(entity);
        
        assertTrue(repo.existsByUsername("uniqueuser"));
        assertFalse(repo.existsByUsername("notpresent"));
    }
    
    @Test
    void shouldReturnTrueWhenEmailExists() {
        var entity = dataFactory.studentRegistrationEntity("emailuser", "emailuser@example.com");
        repo.saveAndFlush(entity);
        
        assertTrue(repo.existsByEmail("emailuser@example.com"));
        assertFalse(repo.existsByEmail("absent@example.com"));
    }
    
}
