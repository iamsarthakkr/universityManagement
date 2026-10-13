package com.sarthak.universityManagement.ratelimit;

import com.sarthak.universityManagement.common.exceptions.TooManyRequestsException;
import com.sarthak.universityManagement.common.rest.ApiErrorResponse;
import com.sarthak.universityManagement.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

// Not a bean: as one, Spring Boot would also register it as a servlet filter and count every request twice
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Logger LOG = LogManager.getLogger(RateLimitFilter.class);

    private static final RequestMatcher LOGIN = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/auth/login");
    private static final RequestMatcher REGISTRATION = new OrRequestMatcher(
        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/student-registrations"),
        PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/instructor-registrations")
    );
    private static final RequestMatcher UNLIMITED = new OrRequestMatcher(
        PathPatternRequestMatcher.pathPattern(HttpMethod.OPTIONS, "/**"),
        PathPatternRequestMatcher.pathPattern("/actuator/health"),
        PathPatternRequestMatcher.pathPattern("/actuator/info")
    );

    private final RateLimiter rateLimiter;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiter rateLimiter, JwtService jwtService, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return UNLIMITED.matches(request);
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            acquire(request);
        } catch (TooManyRequestsException ex) {
            LOG.warn("Rate limited {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiErrorResponse.errorMessage(ex.getMessage()));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void acquire(HttpServletRequest request) {
        var ip = request.getRemoteAddr();
        if (LOGIN.matches(request)) {
            rateLimiter.acquire(RateLimiter.Limit.LOGIN, ip);
        } else if (REGISTRATION.matches(request)) {
            rateLimiter.acquire(RateLimiter.Limit.REGISTRATION, ip);
        } else {
            var userId = userId(request);
            if (userId != null) {
                rateLimiter.acquire(RateLimiter.Limit.AUTHENTICATED, userId.toString());
            } else {
                rateLimiter.acquire(RateLimiter.Limit.ANONYMOUS, ip);
            }
        }
    }

    private Integer userId(HttpServletRequest request) {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        try {
            return jwtService.extractUserId(header.substring(7));
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
