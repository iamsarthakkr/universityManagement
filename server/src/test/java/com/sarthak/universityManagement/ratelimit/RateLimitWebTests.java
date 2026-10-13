package com.sarthak.universityManagement.ratelimit;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.MutableTestClock;
import com.sarthak.universityManagement.config.WebIntegrationTests;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
    "app.rate-limit.enabled=true",
    "app.rate-limit.login.requests=3",
    "app.rate-limit.login.period=1m",
    "app.rate-limit.login-failures.requests=2",
    "app.rate-limit.login-failures.period=15m",
    "app.rate-limit.registration.requests=2",
    "app.rate-limit.registration.period=1h",
    "app.rate-limit.authenticated.requests=3",
    "app.rate-limit.authenticated.period=1m",
    "app.rate-limit.anonymous.requests=3",
    "app.rate-limit.anonymous.period=1m",
})
class RateLimitWebTests extends WebIntegrationTests {
    @Autowired
    private RateLimiter rateLimiter;
    @Autowired
    private MutableTestClock clock;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clearBuckets() {
        rateLimiter.clear();
    }

    @Test
    void shouldRefuseTheFourthLoginFromOneIpWithARetryAfter() throws Exception {
        for (int i = 0; i < 3; i++) {
            login("nobody" + i, "wrong", "10.0.0.1").andExpect(status().isUnauthorized());
        }

        login("nobody3", "wrong", "10.0.0.1")
            .andExpect(status().isTooManyRequests())
            .andExpect(header().string(HttpHeaders.RETRY_AFTER, "20"))
            .andExpect(jsonPath("$.isSuccess").value(false))
            .andExpect(jsonPath("$.message").value("Too many requests. Try again in 20 seconds."));
        login("nobody3", "wrong", "10.0.0.2").andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLimitFailedLoginsPerUsernameAcrossIps() throws Exception {
        login("ada", "wrong", "10.0.0.1").andExpect(status().isUnauthorized());
        login("ada", "wrong", "10.0.0.2").andExpect(status().isUnauthorized());

        login("ADA ", "wrong", "10.0.0.3")
            .andExpect(status().isTooManyRequests())
            .andExpect(header().string(HttpHeaders.RETRY_AFTER, "450"))
            .andExpect(jsonPath("$.message").value("Too many failed login attempts. Try again in 8 minutes."));
        login("grace", "wrong", "10.0.0.3").andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCountSuccessfulLoginsAgainstTheUsername() throws Exception {
        userSeeder.saveUser(UserFixtures.user()
            .username("riya")
            .email("riya@example.com")
            .password(passwordEncoder.encode("secret"))
            .role(Role.ADMIN)
            .build());

        for (int i = 0; i < 3; i++) {
            login("riya", "secret", "10.0.1." + i).andExpect(status().isOk());
        }
        login("riya", "wrong", "10.0.2.1").andExpect(status().isUnauthorized());
        login("riya", "wrong", "10.0.2.2").andExpect(status().isUnauthorized());
        login("riya", "secret", "10.0.2.3").andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldLimitEachUserSeparatelyOnASharedIp() throws Exception {
        var first = bearer(Role.ADMIN);
        var second = bearer(Role.ADMIN);
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/me").with(first).with(ip("10.0.0.1"))).andExpect(status().isOk());
        }

        mockMvc.perform(get("/me").with(first).with(ip("10.0.0.1"))).andExpect(status().isTooManyRequests());
        mockMvc.perform(get("/me").with(second).with(ip("10.0.0.1"))).andExpect(status().isOk());
    }

    @Test
    void shouldCountAnInvalidTokenPerIpAndStillAnswer401() throws Exception {
        RequestPostProcessor badToken = request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer not-a-token");
            return request;
        };
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/me").with(badToken).with(ip("10.0.0.1"))).andExpect(status().isUnauthorized());
        }

        mockMvc.perform(get("/departments").with(ip("10.0.0.1"))).andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldLimitRegistrationRequestsPerIp() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(register(i)).andExpect(status().isBadRequest());
        }

        mockMvc.perform(register(2)).andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldAllowRequestsAgainOnceTheBucketRefills() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/departments").with(ip("10.0.0.1"))).andExpect(status().isOk());
        }
        mockMvc.perform(get("/departments").with(ip("10.0.0.1"))).andExpect(status().isTooManyRequests());

        clock.advance(Duration.ofSeconds(20));

        mockMvc.perform(get("/departments").with(ip("10.0.0.1"))).andExpect(status().isOk());
    }

    @Test
    void shouldTellClientsApartByTheirForwardedAddress() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/departments").header("X-Forwarded-For", "203.0.113.1")).andExpect(status().isOk());
        }

        mockMvc.perform(get("/departments").header("X-Forwarded-For", "203.0.113.1")).andExpect(status().isTooManyRequests());
        mockMvc.perform(get("/departments").header("X-Forwarded-For", "203.0.113.2")).andExpect(status().isOk());
    }

    @Test
    void shouldNeverLimitHealthChecks() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/actuator/health").with(ip("10.0.0.1"))).andExpect(status().isOk());
        }
    }

    private ResultActions login(String username, String password, String ip) throws Exception {
        return mockMvc.perform(post("/auth/login")
            .with(ip(ip))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
    }

    private MockHttpServletRequestBuilder register(int attempt) {
        return post(attempt % 2 == 0 ? "/student-registrations" : "/instructor-registrations")
            .with(ip("10.0.0.1"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}");
    }

    private static RequestPostProcessor ip(String address) {
        return request -> {
            request.setRemoteAddr(address);
            return request;
        };
    }
}
