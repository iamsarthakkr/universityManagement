package com.sarthak.universityManagement.enrollment.converter;

import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class StringToEnrollmentActionConverterTests {
    private final StringToEnrollmentActionConverter converter = new StringToEnrollmentActionConverter();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(value = {
        "approve|APPROVE",
        "APPROVE|APPROVE",
        "Reject|REJECT",
        "' Drop '|DROP",
        "cancel|CANCEL",
    }, delimiter = '|')
    void shouldConvertCaseInsensitivelyAndIgnoreSurroundingSpaces(String input, EnrollmentAction expected) {
        assertEquals(expected, converter.convert(input));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"foo", "approved", ""})
    void shouldRejectUnknownActions(String input) {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}
