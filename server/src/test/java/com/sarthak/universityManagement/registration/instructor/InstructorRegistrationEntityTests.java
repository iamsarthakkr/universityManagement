package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.config.JpaConfig;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
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
@ActiveProfiles("test")
@Import(RegistrationTestConfig.class)
@Transactional
class InstructorRegistrationEntityTests {

    @Autowired
    private InstructorRegistrationRepo repo;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    private InstructorRegistrationEntity.InstructorRegistrationEntityBuilder instructorRegistrationBuilder;

    @BeforeEach
    void setUp() {
        var department = departmentSeeder.saveDefault("test-department");
        instructorRegistrationBuilder = InstructorRegistrationFixtures
                .instructorRegistration()
                .department(department);
    }

    @Test
    void shouldSaveRegistrationSuccessfully() {
        var saved = repo.saveAndFlush(instructorRegistrationBuilder.build());
        assertNotNull(saved.getId());
        assertTrue(repo.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        var first = instructorRegistrationBuilder
                .username("duplicate")
                .email("email-2@abc")
                .build();
        repo.saveAndFlush(first);

        var duplicate = instructorRegistrationBuilder
                .username("duplicate")
                .email("email-2@abc")
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        var first = instructorRegistrationBuilder
                .username("u-1")
                .email("dupe@abc")
                .build();
        repo.saveAndFlush(first);
        var duplicate = instructorRegistrationBuilder
                .username("u-2")
                .email("dupe@abc")
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(duplicate));
    }

    @Test
    void shouldRejectNullUsername() {
        var entity = instructorRegistrationBuilder.username("temp").email("temp@example.com").build();
        entity.setUsername(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }

    @Test
    void shouldRejectNullEmail() {
        var entity = instructorRegistrationBuilder.username("temp2").email("temp2@example.com").build();
        entity.setEmail(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }

    @Test
    void shouldRejectNullPassword() {
        var entity = instructorRegistrationBuilder.username("temp3").email("temp3@example.com").build();
        entity.setPassword(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }

    @Test
    void shouldRejectNullFirstName() {
        var entity = instructorRegistrationBuilder.username("temp4").email("temp4@example.com").build();
        entity.setFirstName(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }

    @Test
    void shouldRejectNullDepartment() {
        var entity = instructorRegistrationBuilder.username("temp6").email("temp6@example.com").build();
        entity.setDepartment(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }

    @Test
    void shouldRejectNullRegistrationStatus() {
        var entity = instructorRegistrationBuilder.username("temp7").email("temp7@example.com").build();
        entity.setRegistrationStatus(null);
        assertThrows(DataIntegrityViolationException.class, () -> repo.saveAndFlush(entity));
    }

    @Test
    void shouldPopulateCreatedAtBeforeNow() {
        var entity = instructorRegistrationBuilder.username("audit1").email("audit1@example.com").build();
        var saved = repo.saveAndFlush(entity);
        Instant createdInstant = saved.getCreatedAt();
        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        assertTrue(createdInstant.isBefore(Instant.now()));
    }

    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        var entity = instructorRegistrationBuilder.username("audit2").email("audit2@example.com").build();
        var saved = repo.saveAndFlush(entity);
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        assertTrue(updatedInstant.isBefore(Instant.now()));
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        var entity = instructorRegistrationBuilder.username("uniqueuser").email("uniqueuser@example.com").build();
        repo.saveAndFlush(entity);

        assertTrue(repo.existsByUsername("uniqueuser"));
        assertFalse(repo.existsByUsername("notpresent"));
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        var entity = instructorRegistrationBuilder.username("emailuser").email("emailuser@example.com").build();
        repo.saveAndFlush(entity);

        assertTrue(repo.existsByEmail("emailuser@example.com"));
        assertFalse(repo.existsByEmail("absent@example.com"));
    }
}
