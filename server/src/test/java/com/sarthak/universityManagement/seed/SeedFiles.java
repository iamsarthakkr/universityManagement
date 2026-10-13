package com.sarthak.universityManagement.seed;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class SeedFiles {
    private SeedFiles() {}

    static byte[] example() {
        try {
            return new ClassPathResource("seed/seed.example.json").getContentAsByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static byte[] json(String json) {
        return json.getBytes(StandardCharsets.UTF_8);
    }

    static String minimal(String departmentCode) {
        return """
            { "formatVersion": 2, "departments": [ { "code": "%s", "name": "Minimal" } ] }
            """.formatted(departmentCode);
    }
}
