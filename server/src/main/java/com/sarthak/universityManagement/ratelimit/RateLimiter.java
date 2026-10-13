package com.sarthak.universityManagement.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sarthak.universityManagement.common.exceptions.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class RateLimiter {
    public enum Limit { LOGIN, LOGIN_FAILURES, REGISTRATION, AUTHENTICATED, ANONYMOUS }

    private final RateLimitProperties properties;
    private final TimeMeter timeMeter;
    private final Cache<String, Bucket> buckets;

    public RateLimiter(RateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.timeMeter = new TimeMeter() {
            @Override
            public long currentTimeNanos() {
                return TimeUnit.MILLISECONDS.toNanos(clock.millis());
            }

            @Override
            public boolean isWallClockBased() {
                return true;
            }
        };
        var longestPeriod = Stream.of(Limit.values()).map(limit -> limitFor(limit).period()).max(Duration::compareTo).orElseThrow();
        this.buckets = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(longestPeriod)
            .build();
    }

    public void acquire(Limit limit, String key) {
        if (!properties.enabled()) {
            return;
        }
        var probe = bucket(limit, key).tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            throw tooMany(limit, probe.getNanosToWaitForRefill());
        }
    }

    public void ensureAvailable(Limit limit, String key) {
        if (!properties.enabled()) {
            return;
        }
        var probe = bucket(limit, key).estimateAbilityToConsume(1);
        if (!probe.canBeConsumed()) {
            throw tooMany(limit, probe.getNanosToWaitForRefill());
        }
    }

    public void record(Limit limit, String key) {
        if (properties.enabled()) {
            bucket(limit, key).tryConsume(1);
        }
    }

    void clear() {
        buckets.invalidateAll();
    }

    private Bucket bucket(Limit limit, String key) {
        return buckets.get(limit + ":" + key, k -> {
            var config = limitFor(limit);
            return Bucket.builder()
                .addLimit(bandwidth -> bandwidth.capacity(config.requests()).refillGreedy(config.requests(), config.period()))
                .withCustomTimePrecision(timeMeter)
                .build();
        });
    }

    private RateLimitProperties.Limit limitFor(Limit limit) {
        return switch (limit) {
            case LOGIN -> properties.login();
            case LOGIN_FAILURES -> properties.loginFailures();
            case REGISTRATION -> properties.registration();
            case AUTHENTICATED -> properties.authenticated();
            case ANONYMOUS -> properties.anonymous();
        };
    }

    private static TooManyRequestsException tooMany(Limit limit, long nanosToWait) {
        var seconds = Math.max(1, (long) Math.ceil(nanosToWait / 1e9));
        var wait = seconds < 120 ? seconds + " seconds" : (seconds + 59) / 60 + " minutes";
        var message = limit == Limit.LOGIN_FAILURES
            ? "Too many failed login attempts. Try again in " + wait + "."
            : "Too many requests. Try again in " + wait + ".";
        return new TooManyRequestsException(message, seconds);
    }
}
