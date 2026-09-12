package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.config.JpaConfig;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@ActiveProfiles("test")
public class SemesterRepoTests {

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
    void shouldRejectInvalidDates() {
        assertThrows(DataIntegrityViolationException.class, () -> semesterRepo.saveAndFlush(
            SemesterFixtures.semester()
                .registrationStartDate(LocalDate.of(2026, 1, 20))
                .registrationEndDate(LocalDate.of(2026, 1, 19))
                .build()
        ));

        assertThrows(DataIntegrityViolationException.class, () -> semesterRepo.saveAndFlush(
            SemesterFixtures.semester()
                .startDate(LocalDate.of(2026, 1, 20))
                .endDate(LocalDate.of(2026, 1, 19))
                .build()
        ));
    }
}
