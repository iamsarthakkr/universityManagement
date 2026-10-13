package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.seed.dto.SeedFile;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Component
public class SeedFileReader {
    // Strict on purpose: Spring's shared mapper ignores unknown fields, which would silently drop a renamed one
    private static final JsonMapper STRICT_MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .build();

    private final Validator validator;

    public SeedFileReader(Validator validator) {
        this.validator = validator;
    }

    public SeedFile parse(byte[] content) {
        SeedFile seedFile;
        try {
            seedFile = STRICT_MAPPER.readValue(content, SeedFile.class);
        } catch (JacksonException e) {
            throw new SeedException("Seed file is not valid: " + e.getOriginalMessage());
        }

        if (seedFile.formatVersion() != null && seedFile.formatVersion() != SeedFile.CURRENT_FORMAT_VERSION) {
            throw new SeedException(
                "Seed file is formatVersion " + seedFile.formatVersion()
                    + ", but this build expects " + SeedFile.CURRENT_FORMAT_VERSION
                    + ". Update it to match seed.example.json."
            );
        }

        var violations = validator.validate(seedFile);
        if (!violations.isEmpty()) {
            var details = violations.stream()
                .map(SeedFileReader::describe)
                .sorted()
                .collect(Collectors.joining("; "));
            throw new SeedException("Seed file is not valid: " + details);
        }
        return seedFile;
    }

    public static String checksum(byte[] content) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(content);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String describe(ConstraintViolation<SeedFile> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }
}
