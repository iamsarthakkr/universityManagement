package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.InstructorRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RegistrationTestConfig.class)
@Transactional
public class InstructorRegistrationAdminServiceIntegrationTests {
    @Autowired
    private InstructorRegistrationService service;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private InstructorRepo instructorRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InstructorRegistrationSeeder instructorRegistrationSeeder;
    @Autowired
    private UserSeeder userSeeder;

    private final String adminUsername = "admin";
    private final String adminEmail = "admin@gmail.com";

    @BeforeEach
    void beforeEach() {
        var user = userSeeder.saveUser(
                UserFixtures.user().username(adminUsername).email(adminEmail).role(Role.ADMIN).build()
        );
        TestSecurityUtils.authenticateAs(user);
    }

    @AfterEach
    void afterEach() {
        TestSecurityUtils.clearAuthentication();
    }

    @Nested
    class Registration {
        @Test
        void shouldApprovePendingInstructorRegistration() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            var updated = instructorRegistrationRepo.findById(saved.getId()).orElseThrow();
            assertEquals(RegistrationStatus.APPROVED, updated.getRegistrationStatus());
        }

    }

    @Nested
    class ApproveInstructorRegistration {

        @Test
        void shouldSetCorrectReviewerAndReviewedAtWhenApproved() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            var updated = instructorRegistrationRepo.findById(saved.getId()).orElseThrow();
            assertEquals(adminUsername, updated.getReviewedBy().getUsername());
            assertEquals(adminEmail, updated.getReviewedBy().getEmail());
            assertTrue(updated.getReviewedAt().isBefore(Instant.now()));
        }

        @Test
        void shouldCreateUserWhenRegistrationApproved() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            assertTrue(userRepo.existsByUsername(saved.getUsername()));
            assertTrue(userRepo.existsByEmail(saved.getEmail()));
        }

        @Test
        void shouldCreateInstructorWhenRegistrationApproved() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            var user = userRepo.findByUsername(saved.getUsername()).orElseThrow();
            assertTrue(instructorRepo.existsByUserId(user.getId()));
        }

        @Test
        void shouldCreateInstructorWithCorrectDepartmentWhenRegistrationApproved() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            var user = userRepo.findByUsername(saved.getUsername()).orElseThrow();
            var instructor = instructorRepo.findByUserId(user.getId()).orElseThrow();

            assertTrue(instructorRepo.existsByUserId(user.getId()));
            assertEquals(instructor.getDepartment().getCode(), saved.getDepartment().getCode());
        }
    }

    @Nested
    class RejectInstructorRegistration {
        @Test
        void shouldMarkRegistrationAsRejected() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.rejectRegistration(saved.getId());

            var updated = instructorRegistrationRepo.findById(saved.getId()).orElseThrow();
            assertEquals(RegistrationStatus.REJECTED, updated.getRegistrationStatus());
        }

        @Test
        void shouldSetCorrectReviewerAndReviewedAtWhenRejected() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.rejectRegistration(saved.getId());

            var updated = instructorRegistrationRepo.findById(saved.getId()).orElseThrow();
            assertEquals(adminUsername, updated.getReviewedBy().getUsername());
            assertEquals(adminEmail, updated.getReviewedBy().getEmail());
            assertTrue(updated.getReviewedAt().isBefore(Instant.now()));
        }

        @Test
        void shouldNotCreateUserWhenRegistrationRejected() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.rejectRegistration(saved.getId());

            assertFalse(userRepo.existsByUsername(saved.getUsername()));
            assertFalse(userRepo.existsByEmail(saved.getEmail()));
            assertEquals(1, userRepo.count());
            assertEquals(0, instructorRepo.count());
        }
    }

    @Nested
    class InvalidApprovals {
        @Test
        void shouldThrowWhenRegistrationNotFound() {
            assertThrows(ResourceNotFoundException.class, () -> service.approveRegistration(Integer.MAX_VALUE));
            assertThrows(ResourceNotFoundException.class, () -> service.rejectRegistration(Integer.MAX_VALUE));
        }

        @Test
        void shouldRejectApprovingApprovedRegistration() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            assertThrows(ConflictException.class, () -> service.approveRegistration(saved.getId()));
        }

        @Test
        void shouldRejectApprovingRejectedRegistration() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.rejectRegistration(saved.getId());

            assertThrows(ConflictException.class, () -> service.approveRegistration(saved.getId()));
        }

        @Test
        void shouldRejectRejectingApprovedRegistration() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.approveRegistration(saved.getId());

            assertThrows(ConflictException.class, () -> service.rejectRegistration(saved.getId()));
        }

        @Test
        void shouldRejectRejectingRejectedRegistration() {
            var saved = instructorRegistrationSeeder.saveDefaultInstructorRegistration("test-department");
            service.rejectRegistration(saved.getId());

            assertThrows(ConflictException.class, () -> service.rejectRegistration(saved.getId()));
        }
    }
}
