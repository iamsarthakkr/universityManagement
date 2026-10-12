package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.MySqlTestContainer;
import com.sarthak.universityManagement.config.TestClockConfig;
import com.sarthak.universityManagement.config.TestUtilsConfiguration;
import com.sarthak.universityManagement.department.DepartmentRepo;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({
    TestUtilsConfiguration.class,
    TestClockConfig.class,
})
public class StudentRegistrationConcurrencyTests extends MySqlTestContainer {
    @Autowired
    private StudentRegistrationService service;
    @Autowired
    private StudentRegistrationRepo registrationRepo;
    @Autowired
    private StudentRegistrationSeeder registrationSeeder;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<Runnable> cleanup = new ArrayList<>();

    @AfterEach
    void tearDown() {
        cleanup.reversed().forEach(Runnable::run);
        cleanup.clear();
    }

    @Test
    void shouldWaitForRowLockAndThenRejectTheSecondReview() throws Exception {
        var department = departmentSeeder.saveDefault("dep-conc");
        cleanup.add(() -> departmentRepo.deleteById(department.getId()));
        var admin = userSeeder.saveUser(UserFixtures.user().username("concurrency-admin").email("concurrency-admin@test.com").role(Role.ADMIN).build());
        cleanup.add(() -> userRepo.deleteById(admin.getId()));
        var registration = registrationSeeder.saveStudentRegistration(StudentRegistrationFixtures.studentRegistration().department(department).build());
        cleanup.add(() -> registrationRepo.deleteById(registration.getId()));

        var executor = Executors.newFixedThreadPool(2);
        var lockHeld = new CountDownLatch(1);
        var release = new CountDownLatch(1);

        try {
            var holder = executor.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
                var locked = registrationRepo.findForUpdateById(registration.getId()).orElseThrow();
                lockHeld.countDown();
                await(release);
                locked.setRegistrationStatus(RegistrationStatus.REJECTED);
            }));
            assertTrue(lockHeld.await(10, TimeUnit.SECONDS), "holder never acquired the lock");

            var contender = executor.submit(() -> {
                TestAuthentication.asUser(admin);
                try {
                    return service.performAction(registration.getId(), RegistrationAction.APPROVE);
                } finally {
                    TestAuthentication.clear();
                }
            });

            assertThrows(TimeoutException.class, () -> contender.get(1, TimeUnit.SECONDS),
                "approval did not wait for the row lock");

            release.countDown();
            holder.get(10, TimeUnit.SECONDS);

            var failure = assertThrows(ExecutionException.class, () -> contender.get(10, TimeUnit.SECONDS));
            assertInstanceOf(ConflictException.class, failure.getCause());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }

        assertEquals(RegistrationStatus.REJECTED, registrationRepo.findById(registration.getId()).orElseThrow().getRegistrationStatus());
        assertFalse(userRepo.existsByUsername(registration.getUsername()), "the losing approval must not create a user");
    }

    private static void await(CountDownLatch latch) {
        try {
            if(!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test never released the lock");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}
