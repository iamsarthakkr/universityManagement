package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterIntegrationTests extends IntegrationTests {

    @Autowired
    private SemesterService semesterService;
    @Autowired
    private SemesterSeeder semesterSeeder;

    @Nested
    class CreationTests {

        @Test
        @WithAdmin
        void shouldCreateSemesterWithCorrectFields() {
            var semesterRequest = SemesterFixtures.semesterRequest().build();

            var resp = semesterService.createSemester(semesterRequest);

            assertNotNull(resp);

            assertEquals(resp.term(), semesterRequest.term());
            assertEquals(resp.year(), semesterRequest.year());
            assertEquals(resp.registrationStartDate(), semesterRequest.registrationStartDate());
            assertEquals(resp.registrationEndDate(), semesterRequest.registrationEndDate());
            assertEquals(resp.startDate(), semesterRequest.startDate());
            assertEquals(resp.endDate(), semesterRequest.endDate());

            assertEquals(SemesterStatus.PLANNED, resp.status());
        }

    }

    @Nested
    class FetchTests {

        @Test
        void shouldFetchSemester() {
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);

            var got = semesterService.getSemester(sem.getId());

            assertNotNull(got);
            assertEquals(sem.getTerm(), got.term());
            assertEquals(sem.getYear(), got.year());
        }

        @Test
        void shouldFetchAllSemester() {
            semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2026);
            semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2027);
            semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2027);

            var got = semesterService.getSemesters();

            assertNotNull(got);
            assertEquals(4, got.size());
        }

        @Test
        void shouldThrowWhenSemesterNotFound() {
            assertThrows(ResourceNotFoundException.class, () -> semesterService.getSemester(-1));
        }
    }

    @Nested
    class TransitionTests {

        @ParameterizedTest
        @CsvSource({
            "PLANNED, ACTIVE",
            "PLANNED, CANCELLED",
            "ACTIVE, COMPLETED",
            "ACTIVE, CANCELLED",
        })
        @WithAdmin
        void shouldAllowSemesterTransition(SemesterStatus from, SemesterStatus to) {
            var sem = semesterSeeder.saveSemester(SemesterFixtures.semester().status(from).build());

            semesterService.transition(sem.getId(), to);
            var updated = semesterService.getSemester(sem.getId());

            assertEquals(to, updated.status());
        }

        @ParameterizedTest
        @CsvSource({
            "PLANNED, PLANNED",
            "PLANNED, COMPLETED",
            "ACTIVE, PLANNED",
            "ACTIVE, ACTIVE",
            "COMPLETED, PLANNED",
            "COMPLETED, ACTIVE",
            "COMPLETED, CANCELLED",
            "COMPLETED, COMPLETED",
            "CANCELLED, PLANNED",
            "CANCELLED, ACTIVE",
            "CANCELLED, COMPLETED",
            "CANCELLED, CANCELLED",
        })
        @WithAdmin
        void shouldDenySemesterTransition(SemesterStatus from, SemesterStatus to) {
            var sem = semesterSeeder.saveSemester(SemesterFixtures.semester().status(from).build());

            assertThrows(BadRequestException.class,  () -> semesterService.transition(sem.getId(), to));
            var updated = semesterService.getSemester(sem.getId());

            assertEquals(from, updated.status()); // should preserve original status
        }
    }
}
