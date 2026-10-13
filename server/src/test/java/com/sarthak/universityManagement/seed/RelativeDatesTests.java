package com.sarthak.universityManagement.seed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class RelativeDatesTests {
    private static final LocalDate ANCHOR = LocalDate.of(2026, 1, 10);

    @ParameterizedTest
    @CsvSource({
        "+14d, 2026-01-24",
        "14d, 2026-01-24",
        "-10d, 2025-12-31",
        "0d, 2026-01-10",
        "2027-03-01, 2027-03-01",
    })
    void shouldResolveOffsetsAndIsoDates(String value, LocalDate expected) {
        assertEquals(expected, RelativeDates.resolve(value, ANCHOR, "field"));
    }

    @Test
    void shouldNameTheFieldWhenTheValueIsNeither() {
        var exception = assertThrows(SeedException.class, () -> RelativeDates.resolve("+2w", ANCHOR, "semesters[x].startDate"));

        assertTrue(exception.getMessage().startsWith("semesters[x].startDate: '+2w'"));
    }
}
