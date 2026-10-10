package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.MySqlTestContainer;
import com.sarthak.universityManagement.config.TestClockConfig;
import com.sarthak.universityManagement.config.TestUtilsConfiguration;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
public class SemesterConcurrencyTests extends MySqlTestContainer {
    @Autowired
    private SemesterService semesterService;
    @Autowired
    private SemesterRepo semesterRepo;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @AfterEach
    void tearDown() {
        semesterRepo.deleteAll();
    }

    @Test
    void shouldWaitForRowLockAndThenSeeTheCommittedStatus() throws Exception {
        var semesterId = semesterSeeder
            .saveSemester(SemesterFixtures.semester().status(SemesterStatus.ACTIVE).build())
            .getId();

        var executor = Executors.newFixedThreadPool(2);
        var lockHeld = new CountDownLatch(1);
        var release = new CountDownLatch(1);

        try {
            // Holds the same row lock a transition takes, then completes the semester when released
            var holder = executor.submit(() -> new TransactionTemplate(transactionManager).executeWithoutResult(tx -> {
                var semester = semesterRepo.findForUpdateById(semesterId).orElseThrow();
                lockHeld.countDown();
                await(release);
                semester.setStatus(SemesterStatus.COMPLETED);
            }));
            assertTrue(lockHeld.await(10, TimeUnit.SECONDS), "holder never acquired the lock");

            var contender = executor.submit(() -> {
                TestAuthentication.asRole(Role.ADMIN);
                try {
                    return semesterService.transition(semesterId, SemesterAction.CANCEL);
                } finally {
                    TestAuthentication.clear();
                }
            });

            // Without the lock, CANCEL would read ACTIVE and finish straight away
            assertThrows(TimeoutException.class, () -> contender.get(1, TimeUnit.SECONDS),
                "transition did not wait for the row lock");

            release.countDown();
            holder.get(10, TimeUnit.SECONDS);

            // Once unblocked, the lock query re-reads the row: COMPLETED cannot be cancelled
            var failure = assertThrows(ExecutionException.class, () -> contender.get(10, TimeUnit.SECONDS));
            assertInstanceOf(BadRequestException.class, failure.getCause());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }

        assertEquals(SemesterStatus.COMPLETED, semesterRepo.findById(semesterId).orElseThrow().getStatus());
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
