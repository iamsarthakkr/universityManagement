package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.config.RepoTests;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StudentRegistrationRepoTests extends RepoTests {
    @Autowired
    private Clock clock;

    
    @Autowired
    private StudentRegistrationRepo repo;

    @Autowired
    private DepartmentSeeder departmentSeeder;

    private StudentRegistrationEntity.StudentRegistrationEntityBuilder studentRegistrationEntityBuilder;

    @BeforeEach
    void setUp() {
        var department = departmentSeeder.saveDefault("dep-test");
        studentRegistrationEntityBuilder = StudentRegistrationFixtures.studentRegistration().department(department);
    }

    @Test
    void shouldSaveRegistrationSuccessfully() {
        var entity = studentRegistrationEntityBuilder.username("student1").email("student1@example.com").build();
        var saved = repo.saveAndFlush(entity);

        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }
    
    @Test
    void shouldAllowRepeatedUsernameAndEmailAcrossRequests() {
        repo.saveAndFlush(studentRegistrationEntityBuilder.username("repeat").email("repeat@example.com").registrationStatus(RegistrationStatus.REJECTED).build());

        assertDoesNotThrow(() -> repo.saveAndFlush(studentRegistrationEntityBuilder.username("repeat").email("repeat@example.com").registrationStatus(RegistrationStatus.PENDING).build()));
    }
    
    @Test
    void shouldRejectNullUsername() {
        var entity = studentRegistrationEntityBuilder.username("temp").email("temp@example.com").build();
        entity.setUsername(null);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullEmail() {
        var entity = studentRegistrationEntityBuilder.username("temp2").email("temp2@example.com").build();
        entity.setEmail(null);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullPassword() {
        var entity = studentRegistrationEntityBuilder.username("temp3").email("temp3@example.com").build();
        entity.setPassword(null);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullFirstName() {
        var entity = studentRegistrationEntityBuilder.username("temp4").email("temp4@example.com").build();
        entity.setFirstName(null);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullDateOfBirth() {
        var entity = studentRegistrationEntityBuilder.username("temp6").email("temp6@example.com").build();
        entity.setDateOfBirth(null);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldRejectNullRegistrationStatus() {
        var entity = studentRegistrationEntityBuilder.username("temp10").email("temp10@example.com").build();
        entity.setRegistrationStatus(null);

        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }
    
    @Test
    void shouldPopulateCreatedAtFromClock() {
        var entity = studentRegistrationEntityBuilder.username("audit1").email("audit1@example.com").build();
        var saved = repo.saveAndFlush(entity);
        Instant createdInstant = saved.getCreatedAt();

        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        assertEquals(Instant.now(clock), createdInstant);
    }
    
    @Test
    void shouldPopulateUpdatedAtFromClock() {
        var entity = studentRegistrationEntityBuilder.username("audit2").email("audit2@example.com").build();
        var saved = repo.saveAndFlush(entity);
        Instant updatedInstant = saved.getUpdatedAt();

        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        assertEquals(Instant.now(clock), updatedInstant);
    }
    
    @Test
    void shouldFindUsernameAndEmailOnlyWithMatchingStatus() {
        repo.saveAndFlush(studentRegistrationEntityBuilder.username("pending-user").email("pending@example.com").registrationStatus(RegistrationStatus.PENDING).build());
        repo.saveAndFlush(studentRegistrationEntityBuilder.username("rejected-user").email("rejected@example.com").registrationStatus(RegistrationStatus.REJECTED).build());

        assertTrue(repo.existsByUsernameAndRegistrationStatus("pending-user", RegistrationStatus.PENDING));
        assertTrue(repo.existsByEmailAndRegistrationStatus("pending@example.com", RegistrationStatus.PENDING));
        assertFalse(repo.existsByUsernameAndRegistrationStatus("rejected-user", RegistrationStatus.PENDING));
        assertFalse(repo.existsByEmailAndRegistrationStatus("rejected@example.com", RegistrationStatus.PENDING));
        assertFalse(repo.existsByUsernameAndRegistrationStatus("absent", RegistrationStatus.PENDING));
    }
    
}
