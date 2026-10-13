package com.sarthak.universityManagement.registration.converter;

import com.sarthak.universityManagement.registration.types.RegistrationAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class StringToRegistrationActionConverterTests {
    private final StringToRegistrationActionConverter converter = new StringToRegistrationActionConverter();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(value = {
        "approve|APPROVE",
        "APPROVE|APPROVE",
        "Reject|REJECT",
        "' reject '|REJECT",
    }, delimiter = '|')
    void shouldConvertCaseInsensitivelyAndIgnoreSurroundingSpaces(String input, RegistrationAction expected) {
        assertEquals(expected, converter.convert(input));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"foo", "approved", "pending", ""})
    void shouldRejectUnknownActions(String input) {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}
