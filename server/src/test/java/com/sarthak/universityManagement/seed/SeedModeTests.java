package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.seed.types.SeedMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class SeedModeTests {

    @ParameterizedTest
    @CsvSource({
        "apply, APPLY",
        "' Rollback ', ROLLBACK",
        "rollback-dry-run, ROLLBACK_DRY_RUN",
    })
    void shouldParseThePropertySpelling(String value, SeedMode expected) {
        assertEquals(expected, SeedMode.fromProperty(value));
    }

    @Test
    void shouldListAllowedValuesForAnUnknownMode() {
        var exception = assertThrows(IllegalArgumentException.class, () -> SeedMode.fromProperty("seed"));

        assertEquals("Invalid app.seed.mode 'seed'. Allowed values: apply, rollback-dry-run, rollback", exception.getMessage());
    }
}
