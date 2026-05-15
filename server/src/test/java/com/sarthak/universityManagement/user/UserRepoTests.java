package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.types.Role;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // disable replacing DataSource
@Import(JpaConfig.class)
@ActiveProfiles("test")
class UserRepoTests {
    
    @Autowired
    private UserRepo userRepo;
    
    private UserEntity createUser(String username, String email, Role role) {
        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("secret"); // adjust if your entity requires other fields
        u.setRole(role);
        u.setActive(true);
        return u;
    }
    
    @BeforeEach
    void cleanup() {
        userRepo.deleteAll();
    }
    
    @Test
    void shouldSaveUserSuccessfully() {
        UserEntity user = createUser("alice", "alice@example.com", Role.STUDENT);
        UserEntity saved = userRepo.save(user);
        assertNotNull(saved.getId(), "Saved user should have an id");
        assertTrue(userRepo.findById(saved.getId()).isPresent(), "User must be retrievable by id");
    }
    
    @Test
    void shouldFindUserByUsername() {
        String username = "bob";
        UserEntity user = createUser(username, "bob@example.com", Role.STUDENT);
        userRepo.saveAndFlush(user);
        
        var found = userRepo.findByUsername(username);
        assertTrue(found.isPresent(), "findByUsername should return the user");
        assertEquals(username, found.get().getUsername());
    }
    
    @Test
    void shouldReturnTrueWhenUsernameExists() {
        String username = "carol";
        UserEntity user = createUser(username, "carol@example.com", Role.STUDENT);
        userRepo.saveAndFlush(user);
        
        assertTrue(userRepo.existsByUsername(username), "existsByUsername should return true for existing username");
    }
    
    @Test
    void shouldReturnTrueWhenEmailExists() {
        String email = "frank@example.com";
        UserEntity user = createUser("frank", email, Role.STUDENT);
        userRepo.saveAndFlush(user);
        
        assertTrue(userRepo.existsByEmail(email), "existsByEmail should return true for existing email");
    }
    
    @Test
    void shouldRejectDuplicateUsername() {
        String username = "dave";
        UserEntity first = createUser(username, "dave1@example.com", Role.STUDENT);
        userRepo.saveAndFlush(first);
        
        UserEntity duplicate = createUser(username, "dave2@example.com", Role.STUDENT);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(duplicate),
            "Saving a user with duplicate username should fail with a DataIntegrityViolationException");
    }
    
    @Test
    void shouldRejectDuplicateEmail() {
        String email = "erin@example.com";
        UserEntity first = createUser("erin1", email, Role.STUDENT);
        userRepo.saveAndFlush(first);
        
        UserEntity duplicate = createUser("erin2", email, Role.STUDENT);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(duplicate),
            "Saving a user with duplicate email should fail with a DataIntegrityViolationException");
    }
    
    @Test
    void shouldPersistUserRole() {
        Role role = Role.ADMIN;
        UserEntity user = createUser("greg", "greg@example.com", role);
        UserEntity saved = userRepo.saveAndFlush(user);
        
        assertNotNull(saved.getId(), "Saved user should have an id");
        assertEquals(role, saved.getRole(), "Persisted user should retain the assigned role");
    }
    
    @Test
    void shouldRejectNullUsername() {
        UserEntity u = createUser("temp", "temp@example.com", Role.STUDENT);
        u.setUsername(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldRejectNullEmail() {
        UserEntity u = createUser("temp2", "temp2@example.com", Role.STUDENT);
        u.setEmail(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldRejectNullPassword() {
        UserEntity u = createUser("temp3", "temp3@example.com", Role.STUDENT);
        u.setPassword(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldRejectNullRole() {
        UserEntity u = createUser("temp4", "temp4@example.com", Role.STUDENT);
        u.setRole(null);
        assertThrows(DataIntegrityViolationException.class, () -> userRepo.saveAndFlush(u));
    }
    
    @Test
    void shouldPopulateCreatedAtBeforeNow() {
        UserEntity user = createUser("audit1", "audit1@example.com", Role.STUDENT);
        UserEntity saved = userRepo.saveAndFlush(user);
        
        Instant createdInstant = saved.getCreatedAt();
        assertNotNull(createdInstant, "createdAt should be populated by auditing");
        Instant now = Instant.now();
        assertTrue(createdInstant.isBefore(now), "createdAt should be at or before current instant");
    }
    
    @Test
    void shouldPopulateUpdatedAtBeforeNow() {
        UserEntity user = createUser("audit2", "audit2@example.com", Role.STUDENT);
        UserEntity saved = userRepo.saveAndFlush(user);
        
        Instant updatedInstant = saved.getUpdatedAt();
        assertNotNull(updatedInstant, "updatedAt should be populated by auditing");
        Instant now = Instant.now();
        assertTrue(updatedInstant.isBefore(now), "updatedAt should be at or before current instant");
    }
    
}