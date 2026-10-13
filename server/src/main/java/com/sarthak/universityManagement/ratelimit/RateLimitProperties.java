package com.sarthak.universityManagement.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(
    boolean enabled,
    Limit login,
    Limit loginFailures,
    Limit registration,
    Limit authenticated,
    Limit anonymous
) {
    public record Limit(int requests, Duration period) {}
}
