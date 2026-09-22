package com.sarthak.universityManagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@TestConfiguration
@ActiveProfiles("test")
public class TestClockConfig {

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(
            Instant.parse("2026-01-10T00:00:00Z"),
            ZoneOffset.UTC
        );
    }
}