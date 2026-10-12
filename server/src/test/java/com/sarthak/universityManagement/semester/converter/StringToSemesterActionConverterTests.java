package com.sarthak.universityManagement.semester.converter;

import com.sarthak.universityManagement.semester.types.SemesterAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class StringToSemesterActionConverterTests {
    private final StringToSemesterActionConverter converter = new StringToSemesterActionConverter();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(value = {
        "activate|ACTIVATE",
        "ACTIVATE|ACTIVATE",
        "Complete|COMPLETE",
        "' cancel '|CANCEL",
    }, delimiter = '|')
    void shouldConvertCaseInsensitivelyAndIgnoreSurroundingSpaces(String input, SemesterAction expected) {
        assertEquals(expected, converter.convert(input));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"foo", "plan", "activated", ""})
    void shouldRejectUnknownActions(String input) {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}
