package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(SemesterSeeder.class)
public class SemesterIntegrationTests {

    @Autowired
    private SemesterService semesterService;
    @Autowired
    private SemesterSeeder semesterSeeder;

    @Nested
    class CreationTests {

        @Test
        @WithAdmin
        void shouldCreateSemesterWithCorrectFields() {
            var semsterRequest = SemesterFixtures.semesterRequest().build();

            var resp = semesterService.createSemester(semsterRequest);

            assertNotNull(resp);

            assertEquals(resp.term(), semsterRequest.term());
            assertEquals(resp.year(), semsterRequest.year());
            assertEquals(resp.registrationStartDate(), semsterRequest.registrationStartDate());
            assertEquals(resp.registrationEndDate(), semsterRequest.registrationEndDate());
            assertEquals(resp.startDate(), semsterRequest.startDate());
            assertEquals(resp.endDate(), semsterRequest.endDate());

            assertEquals(SemesterStatus.PLANNED, resp.status());
        }

        @Test
        @WithAdmin
        void shouldNotCreateSemesterWithDuplicateTermAndYear() {
            var semsterRequest1 = SemesterFixtures.semesterRequest()
                .term(SemesterTerm.SUMMER)
                .year(2026)
                .build();

            var semsterRequest2 = SemesterFixtures.semesterRequest()
                .term(SemesterTerm.SUMMER)
                .year(2026)
                .build();

            semesterService.createSemester(semsterRequest1);

            assertThrows(DataIntegrityViolationException.class, () -> semesterService.createSemester(semsterRequest2));
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
            "COMPLETED, PLANNED",
            "COMPLETED, ACTIVE",
            "COMPLETED, CANCELLED",
            "CANCELLED, PLANNED",
            "CANCELLED, ACTIVE",
            "CANCELLED, COMPLETED",
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
