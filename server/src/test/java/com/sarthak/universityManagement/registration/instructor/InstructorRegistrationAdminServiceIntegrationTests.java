package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.config.MutableTestClock;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class InstructorRegistrationAdminServiceIntegrationTests extends IntegrationTests {
    private static final RegistrationFilter NO_FILTER = new RegistrationFilter(null, null);

    @Autowired
    private InstructorRegistrationService service;
    @Autowired
    private InstructorRegistrationRepo registrationRepo;
    @Autowired
    private InstructorRepo instructorRepo;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private InstructorRegistrationSeeder registrationSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private MutableTestClock testClock;
    @Autowired
    private Clock clock;

    private UserEntity admin;
    private DepartmentEntity department;
    private int registrationCounter = 0;

    @BeforeEach
    void setUp() {
        // performAction records the reviewer, so the admin must be a real user row, not just a role
        admin = userSeeder.saveUser(UserFixtures.user().username("admin").email("admin@gmail.com").role(Role.ADMIN).build());
        TestAuthentication.asUser(admin);
        department = departmentSeeder.saveDefault("dep-test");
    }

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    private InstructorRegistrationEntity saveRegistration(DepartmentEntity department, RegistrationStatus status) {
        var n = ++registrationCounter;
        return registrationSeeder.saveInstructorRegistration(
            InstructorRegistrationFixtures.instructorRegistration()
                .department(department)
                .registrationStatus(status)
                .username("registrant-" + n)
                .email("registrant-" + n + "@test.com")
                .build()
        );
    }

    private InstructorRegistrationEntity savePending() {
        return saveRegistration(department, RegistrationStatus.PENDING);
    }

    @Nested
    class Approval {

        @Test
        void shouldMarkRegistrationAsApproved() {
            var saved = savePending();

            service.performAction(saved.getId(), RegistrationAction.APPROVE);

            assertEquals(RegistrationStatus.APPROVED, registrationRepo.findById(saved.getId()).orElseThrow().getRegistrationStatus());
        }

        @Test
        void shouldCreateUserAndInstructorFromRegistrationDetails() {
            var saved = savePending();

            service.performAction(saved.getId(), RegistrationAction.APPROVE);

            var user = userRepo.findByUsername(saved.getUsername()).orElseThrow();
            assertEquals(saved.getEmail(), user.getEmail());
            assertEquals(Role.INSTRUCTOR, user.getRole());

            var instructor = instructorRepo.findByUserId(user.getId()).orElseThrow();
            assertEquals(saved.getFirstName(), instructor.getFirstName());
            assertEquals(saved.getLastName(), instructor.getLastName());
            assertEquals(department.getId(), instructor.getDepartment().getId());
        }

        @Test
        void shouldRecordReviewerAndReviewTimeFromClock() {
            var saved = savePending();

            service.performAction(saved.getId(), RegistrationAction.APPROVE);

            var updated = registrationRepo.findById(saved.getId()).orElseThrow();
            assertEquals(admin.getId(), updated.getReviewedBy().getId());
            assertEquals(Instant.now(clock), updated.getReviewedAt());
        }

        @Test
        void shouldReturnReviewedRegistrationWithNoFurtherActions() {
            var saved = savePending();

            var resp = service.performAction(saved.getId(), RegistrationAction.APPROVE);

            assertEquals(RegistrationStatus.APPROVED, resp.status());
            assertEquals(List.of(), resp.allowedActions());
            assertEquals(Instant.now(clock), resp.reviewedAt());
        }
    }

    @Nested
    class Rejection {

        @Test
        void shouldMarkRegistrationAsRejectedAndRecordReviewer() {
            var saved = savePending();

            service.performAction(saved.getId(), RegistrationAction.REJECT);

            var updated = registrationRepo.findById(saved.getId()).orElseThrow();
            assertEquals(RegistrationStatus.REJECTED, updated.getRegistrationStatus());
            assertEquals(admin.getId(), updated.getReviewedBy().getId());
            assertEquals(Instant.now(clock), updated.getReviewedAt());
        }

        @Test
        void shouldNotCreateUserOrInstructor() {
            var saved = savePending();
            var usersBefore = userRepo.count();

            service.performAction(saved.getId(), RegistrationAction.REJECT);

            assertFalse(userRepo.existsByUsername(saved.getUsername()));
            assertEquals(usersBefore, userRepo.count());
            assertEquals(0, instructorRepo.count());
        }
    }

    @Nested
    class InvalidActions {

        @ParameterizedTest
        @EnumSource(RegistrationAction.class)
        void shouldThrowWhenRegistrationNotFound(RegistrationAction action) {
            assertThrows(ResourceNotFoundException.class, () -> service.performAction(Integer.MAX_VALUE, action));
        }

        @ParameterizedTest(name = "{0} then {1}")
        @CsvSource({
            "APPROVE, APPROVE",
            "APPROVE, REJECT",
            "REJECT, APPROVE",
            "REJECT, REJECT",
        })
        void shouldRejectActingOnReviewedRegistration(RegistrationAction first, RegistrationAction second) {
            var saved = savePending();
            service.performAction(saved.getId(), first);
            var usersAfterFirst = userRepo.count();

            assertThrows(ConflictException.class, () -> service.performAction(saved.getId(), second));

            assertEquals(first.getTargetStatus(), registrationRepo.findById(saved.getId()).orElseThrow().getRegistrationStatus());
            assertEquals(usersAfterFirst, userRepo.count());
        }
    }

    @Nested
    class Listing {

        private Set<Integer> ids(List<InstructorRegistrationResponse> registrations) {
            return registrations.stream().map(InstructorRegistrationResponse::id).collect(Collectors.toSet());
        }

        @Test
        void shouldOfferApproveAndRejectOnPendingRegistrations() {
            savePending();

            var pending = service.getRequests(new RegistrationFilter(null, RegistrationStatus.PENDING));

            assertEquals(1, pending.size());
            assertEquals(List.of(RegistrationAction.APPROVE, RegistrationAction.REJECT), pending.getFirst().allowedActions());
        }

        @Test
        void shouldReturnEveryStatusWhenFilterIsEmpty() {
            var pending = savePending();
            var approved = saveRegistration(department, RegistrationStatus.APPROVED);
            var rejected = saveRegistration(department, RegistrationStatus.REJECTED);

            assertEquals(Set.of(pending.getId(), approved.getId(), rejected.getId()), ids(service.getRequests(NO_FILTER)));
        }

        @Test
        void shouldFilterByStatus() {
            var pending = savePending();
            saveRegistration(department, RegistrationStatus.APPROVED);

            assertEquals(Set.of(pending.getId()), ids(service.getRequests(new RegistrationFilter(null, RegistrationStatus.PENDING))));
        }

        @Test
        void shouldFilterByDepartment() {
            var own = savePending();
            saveRegistration(departmentSeeder.saveDefault("other-dep"), RegistrationStatus.PENDING);

            assertEquals(Set.of(own.getId()), ids(service.getRequests(new RegistrationFilter(department.getId(), null))));
        }

        @Test
        void shouldCombineFiltersWithAnd() {
            var match = savePending();
            saveRegistration(department, RegistrationStatus.APPROVED);
            saveRegistration(departmentSeeder.saveDefault("other-dep"), RegistrationStatus.PENDING);

            var filter = new RegistrationFilter(department.getId(), RegistrationStatus.PENDING);

            assertEquals(Set.of(match.getId()), ids(service.getRequests(filter)));
        }

        @Test
        void shouldListOldestRequestFirst() {
            var otherDepartment = departmentSeeder.saveDefault("other-dep");
            testClock.setTo(Instant.parse("2026-01-03T00:00:00Z"));
            var newest = savePending();
            testClock.setTo(Instant.parse("2026-01-01T00:00:00Z"));
            var oldest = saveRegistration(otherDepartment, RegistrationStatus.PENDING);
            testClock.setTo(Instant.parse("2026-01-02T00:00:00Z"));
            var middle = savePending();

            var ids = service.getRequests(NO_FILTER).stream().map(InstructorRegistrationResponse::id).toList();

            assertEquals(List.of(oldest.getId(), middle.getId(), newest.getId()), ids);
        }
    }
}
