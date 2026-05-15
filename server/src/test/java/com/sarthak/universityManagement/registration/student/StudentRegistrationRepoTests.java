package com.sarthak.universityManagement.registration.student;

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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@ActiveProfiles("test")
class StudentRegistrationRepoTests {
    
    @Autowired
    private StudentRegistrationRepo repo;
    
    private StudentRegistrationEntity createEntity(String username, String email) {
        return StudentRegistrationEntity.builder()
            .registrationStatus(RegistrationStatus.PENDING)
            .username(username)
            .password("secret")
            .firstName("John")
            .lastName("Doe")
            .email(email)
            .phoneNumber("1234567890")
            .dateOfBirth(LocalDate.of(2000, 1, 1))
            .address("123 Main St")
            .fatherName("Father Name")
            .motherName("Mother Name")
            .build();
    }
    
    @BeforeEach
    void cleanup() {
        repo.deleteAll();
    }
    
    @Test
    void shouldSaveRegistrationSuccessfully() {
        var entity = createEntity("student1", "student1@example.com");
        var saved = repo.saveAndFlush(entity);
        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }
    
    @Test
    void shouldRejectDuplicateUsername() {
        var first = createEntity("student2", "student2a@example.com");
        repo.saveAndFlush(first);
        var duplicate = createEntity("student2", "student2b@example.com");
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        var first = createEntity("student3a", "student3@example.com");
        repo.saveAndFlush(first);
        var duplicate = createEntity("student3b", "student3@example.com");
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
    void shouldRejectNullDateOfBirth() {
        var entity = createEntity("temp6", "temp6@example.com");
        entity.setDateOfBirth(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullAddress() {
        var entity = createEntity("temp7", "temp7@example.com");
        entity.setAddress(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullFatherName() {
        var entity = createEntity("temp8", "temp8@example.com");
        entity.setFatherName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullMotherName() {
        var entity = createEntity("temp9", "temp9@example.com");
        entity.setMotherName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullRegistrationStatus() {
        var entity = createEntity("temp10", "temp10@example.com");
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
    
    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        var entity = createEntity("audit2", "audit2@example.com");
        var saved = repo.saveAndFlush(entity);
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        assertTrue(updatedInstant.isBefore(Instant.now()));
    }
    
}
