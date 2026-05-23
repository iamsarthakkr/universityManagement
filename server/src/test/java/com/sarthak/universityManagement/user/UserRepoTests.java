package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.types.Role;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable replacing DataSource
@Import({JpaConfig.class, TestDataFactory.class})
@ActiveProfiles("test")
@Transactional
class UserRepoTests {
    
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestDataFactory dataFactory;
    
    @Test
    void shouldSaveUserSuccessfully() {
        UserEntity user = dataFactory.user("alice", "alice@example.com", Role.STUDENT);
        UserEntity saved = userRepo.save(user);
        assertNotNull(saved.getId(), "Saved user should have an id");
        assertTrue(userRepo.findById(saved.getId()).isPresent(), "User must be retrievable by id");
    }
    
    @Test
    void shouldFindUserByUsername() {
        String username = "bob";
        UserEntity user = dataFactory.user(username, "bob@example.com", Role.STUDENT);
        userRepo.saveAndFlush(user);
        
        var found = userRepo.findByUsername(username);
        assertTrue(found.isPresent(), "findByUsername should return the user");
        assertEquals(username, found.get().getUsername());
    }
    
    @Test
    void shouldReturnTrueWhenUsernameExists() {
        String username = "carol";
        UserEntity user = dataFactory.user(username, "carol@example.com", Role.STUDENT);
        userRepo.saveAndFlush(user);
        
        assertTrue(userRepo.existsByUsername(username), "existsByUsername should return true for existing username");
    }
    
    @Test
    void shouldReturnTrueWhenEmailExists() {
        String email = "frank@example.com";
        UserEntity user = dataFactory.user("frank", email, Role.STUDENT);
        userRepo.saveAndFlush(user);
        
        assertTrue(userRepo.existsByEmail(email), "existsByEmail should return true for existing email");
    }
    
    @Test
    void shouldRejectDuplicateUsername() {
        String username = "dave";
        UserEntity first = dataFactory.user(username, "dave1@example.com", Role.STUDENT);
        userRepo.saveAndFlush(first);
        
        UserEntity duplicate = dataFactory.user(username, "dave2@example.com", Role.STUDENT);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(duplicate),
            "Saving a user with duplicate username should fail with a DataIntegrityViolationException");
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        String email = "erin@example.com";
        UserEntity first = dataFactory.user("erin1", email, Role.STUDENT);
        userRepo.saveAndFlush(first);
        
        UserEntity duplicate = dataFactory.user("erin2", email, Role.STUDENT);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(duplicate),
            "Saving a user with duplicate email should fail with a DataIntegrityViolationException");
    }
    
    @Test
    void shouldPersistUserRole() {
        Role role = Role.ADMIN;
        UserEntity user = dataFactory.user("greg", "greg@example.com", role);
        UserEntity saved = userRepo.saveAndFlush(user);
        
        assertNotNull(saved.getId(), "Saved user should have an id");
        assertEquals(role, saved.getRole(), "Persisted user should retain the assigned role");
    }
    
    @Test
    void shouldRejectNullUsername() {
        UserEntity u = dataFactory.user("temp", "temp@example.com", Role.STUDENT);
        u.setUsername(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldRejectNullEmail() {
        UserEntity u = dataFactory.user("temp2", "temp2@example.com", Role.STUDENT);
        u.setEmail(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldRejectNullPassword() {
        UserEntity u = dataFactory.user("temp3", "temp3@example.com", Role.STUDENT);
        u.setPassword(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldRejectNullRole() {
        UserEntity u = dataFactory.user("temp4", "temp4@example.com", Role.STUDENT);
        u.setRole(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldPopulateCreatedAtBeforeNow() {
        UserEntity user = dataFactory.user("audit1", "audit1@example.com", Role.STUDENT);
        UserEntity saved = userRepo.saveAndFlush(user);
        
        Instant createdInstant = saved.getCreatedAt();
        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        Instant now = Instant.now();
        assertTrue(createdInstant.isBefore(now), "createdAt should be at or before current instant");
    }
    
    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        UserEntity user = dataFactory.user("audit2", "audit2@example.com", Role.STUDENT);
        UserEntity saved = userRepo.saveAndFlush(user);
        
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        Instant now = Instant.now();
        assertTrue(updatedInstant.isBefore(now), "updatedAt should be at or before current instant");
    }
    
}