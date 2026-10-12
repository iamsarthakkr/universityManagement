package com.sarthak.universityManagement.enrollment.converter;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class StringToEnrollmentStatusConverterTests {
    private final StringToEnrollmentStatusConverter converter = new StringToEnrollmentStatusConverter();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(value = {
        "pending|PENDING",
        "ENROLLED|ENROLLED",
        "Rejected|REJECTED",
        "' cancelled '|CANCELLED",
        "dropped|DROPPED",
    }, delimiter = '|')
    void shouldConvertCaseInsensitivelyAndIgnoreSurroundingSpaces(String input, EnrollmentStatus expected) {
        assertEquals(expected, converter.convert(input));
    }

    // Action names are not statuses: "approve" must not slip through as a filter value
    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"foo", "approve", "drop", ""})
    void shouldRejectUnknownStatuses(String input) {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}
