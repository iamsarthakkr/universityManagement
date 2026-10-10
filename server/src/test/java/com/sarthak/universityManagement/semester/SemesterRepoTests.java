package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.config.RepoTests;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterRepoTests extends RepoTests {
    private static final LocalDate DAY = LocalDate.of(2026, 1, 20);

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

    static Stream<Arguments> invalidDateCases() {
        return Stream.of(
            Arguments.of("registration reversed", SemesterFixtures.semester().registrationStartDate(DAY).registrationEndDate(DAY.minusDays(1)).build()),
            Arguments.of("registration same day", SemesterFixtures.semester().registrationStartDate(DAY).registrationEndDate(DAY).build()),
            Arguments.of("semester reversed", SemesterFixtures.semester().startDate(DAY).endDate(DAY.minusDays(1)).build()),
            Arguments.of("semester same day", SemesterFixtures.semester().startDate(DAY).endDate(DAY).build())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDateCases")
    void shouldRejectInvalidDates(String label, SemesterEntity semester) {
        assertThrows(DataIntegrityViolationException.class, () -> semesterRepo.saveAndFlush(semester));
    }

    @Test
    void shouldFindAndCheckExistenceByTermAndYear() {
        var saved = semesterRepo.saveAndFlush(
            SemesterFixtures.semester().term(SemesterTerm.WINTER).year(2027).build()
        );

        assertTrue(semesterRepo.existsByTermAndYear(SemesterTerm.WINTER, 2027));
        assertEquals(saved.getId(), semesterRepo.findByTermAndYear(SemesterTerm.WINTER, 2027).orElseThrow().getId());

        assertFalse(semesterRepo.existsByTermAndYear(SemesterTerm.SUMMER, 2027));
        assertFalse(semesterRepo.existsByTermAndYear(SemesterTerm.WINTER, 2026));
        assertTrue(semesterRepo.findByTermAndYear(SemesterTerm.SUMMER, 2027).isEmpty());
    }

    @Test
    void shouldOrderByYearAscendingThenTermDescending() {
        // Saved out of order so the assertion can only pass through the query's ORDER BY
        semesterRepo.saveAndFlush(SemesterFixtures.semester().term(SemesterTerm.SUMMER).year(2027).build());
        semesterRepo.saveAndFlush(SemesterFixtures.semester().term(SemesterTerm.SUMMER).year(2026).build());
        semesterRepo.saveAndFlush(SemesterFixtures.semester().term(SemesterTerm.WINTER).year(2027).build());
        semesterRepo.saveAndFlush(SemesterFixtures.semester().term(SemesterTerm.WINTER).year(2026).build());

        var ordered = semesterRepo.findAllByOrderByYearAscTermDesc()
            .stream()
            .map(s -> s.getTerm() + " " + s.getYear())
            .toList();

        assertEquals(List.of("WINTER 2026", "SUMMER 2026", "WINTER 2027", "SUMMER 2027"), ordered);
    }
}
