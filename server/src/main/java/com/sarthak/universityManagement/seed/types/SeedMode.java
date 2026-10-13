package com.sarthak.universityManagement.seed.types;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SeedMode {
    APPLY,
    ROLLBACK_DRY_RUN,
    ROLLBACK;

    public static SeedMode fromProperty(String value) {
        var normalized = value.trim().toUpperCase().replace('-', '_');
        return Arrays.stream(values())
            .filter(mode -> mode.name().equals(normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid app.seed.mode '" + value + "'. Allowed values: " + allowedValues()
            ));
    }

    private static String allowedValues() {
        return Arrays.stream(values())
            .map(mode -> mode.name().toLowerCase().replace('_', '-'))
            .collect(Collectors.joining(", "));
    }
}
