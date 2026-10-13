package com.sarthak.universityManagement.seed;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class RelativeDates {
    private static final Pattern OFFSET = Pattern.compile("([+-]?\\d+)d");

    private RelativeDates() {}

    public static LocalDate resolve(String value, LocalDate anchor, String field) {
        var trimmed = value.trim();
        var offset = OFFSET.matcher(trimmed);
        if (offset.matches()) {
            return anchor.plusDays(Long.parseLong(offset.group(1)));
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException e) {
            throw new SeedException(
                field + ": '" + value + "' is neither an offset like +14d nor a date like 2026-01-31"
            );
        }
    }
}
