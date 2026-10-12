package com.sarthak.universityManagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class TestClockConfig {

    @Bean
    @Primary
    public MutableTestClock testClock() {
        return new MutableTestClock();
    }
}
