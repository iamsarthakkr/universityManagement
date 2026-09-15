package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.testUtils.security.WithStudent;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(SemesterSeeder.class)
public class SemesterAuthorizationTests {

    @Autowired
    private SemesterService semesterService;

    @Autowired
    private SemesterSeeder semesterSeeder;

    @Nested
    class CreationTests {

        @Test
        @WithAdmin
        void adminShouldAllow() {
            var sem = semesterService.createSemester(SemesterFixtures.semesterRequest().build());
            assertNotNull(sem);

        }

        @Test
        @WithStudent
        void studentShouldDeny() {
            assertThrows(AuthorizationDeniedException.class, () -> semesterService.createSemester(SemesterFixtures.semesterRequest().build()));
        }

        @Test
        void unAuthorizedShouldDeny() {
            assertThrows(AuthenticationCredentialsNotFoundException.class, () -> semesterService.createSemester(SemesterFixtures.semesterRequest().build()));
        }
    }

    @Nested
    class TransitionTests {

        private int id;

        @BeforeEach
        @WithAdmin
        void setUp() {
            var saved = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            id = saved.getId();
        }

        @Test
        @WithAdmin
        void adminShouldAllow() {
            semesterService.transition(id, SemesterStatus.ACTIVE);

            var updated = semesterService.getSemester(id);
            assertEquals(SemesterStatus.ACTIVE, updated.status());
        }

        @Test
        @WithStudent
        void studentShouldDeny() {
            assertThrows(AuthorizationDeniedException.class, () -> semesterService.transition(id, SemesterStatus.ACTIVE));
        }

        @Test
        void unauthorizedShouldDeny() {
            assertThrows(AuthenticationCredentialsNotFoundException.class, () -> semesterService.transition(id, SemesterStatus.ACTIVE));
        }
    }
}
