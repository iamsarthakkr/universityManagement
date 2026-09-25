package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.config.RepoTests;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterRepoTests extends RepoTests {

    @Autowired
    private SemesterRepo semesterRepo;

    @Test
    void shouldRejectDuplicateTermAndYear() {
        semesterRepo.saveAndFlush(
            SemesterFixtures.semester().term(SemesterTerm.SUMMER).year(2026).build()
        );

        assertThrows(DataIntegrityViolationException.class, () -> semesterRepo.saveAndFlush(
            SemesterFixtures.semester().term(SemesterTerm.SUMMER).year(2026).build()
        ));
    }

    @Test
    void shouldRejectInvalidRegistrationDates() {
        assertThrows(DataIntegrityViolationException.class, () -> semesterRepo.saveAndFlush(
            SemesterFixtures.semester()
                .registrationStartDate(LocalDate.of(2026, 1, 20))
                .registrationEndDate(LocalDate.of(2026, 1, 19))
                .build()
        ));
    }

    @Test
    void shouldRejectInvalidSemesterDates() {
        assertThrows(DataIntegrityViolationException.class, () -> semesterRepo.saveAndFlush(
            SemesterFixtures.semester()
                .startDate(LocalDate.of(2026, 1, 20))
                .endDate(LocalDate.of(2026, 1, 19))
                .build()
        ));
    }
}
