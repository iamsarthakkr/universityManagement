package com.sarthak.universityManagement.registration.converter;

import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class StringToRegistrationStatusConverterTests {
    private final StringToRegistrationStatusConverter converter = new StringToRegistrationStatusConverter();

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource(value = {
        "pending|PENDING",
        "PENDING|PENDING",
        "Approved|APPROVED",
        "' rejected '|REJECTED",
    }, delimiter = '|')
    void shouldConvertCaseInsensitivelyAndIgnoreSurroundingSpaces(String input, RegistrationStatus expected) {
        assertEquals(expected, converter.convert(input));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"foo", "approve", "reject", ""})
    void shouldRejectUnknownStatuses(String input) {
        assertThrows(IllegalArgumentException.class, () -> converter.convert(input));
    }
}
