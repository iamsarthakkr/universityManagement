package com.sarthak.universityManagement.auth;

import com.sarthak.universityManagement.auth.dto.LoginRequest;
import com.sarthak.universityManagement.auth.dto.LoginResponse;
import com.sarthak.universityManagement.ratelimit.RateLimiter;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.security.jwt.JwtService;
import com.sarthak.universityManagement.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final RateLimiter rateLimiter;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        // Usernames match case-insensitively in MySQL, so "Ada.Lovelace" must share ada.lovelace's limit
        var limitKey = loginRequest.username().trim().toLowerCase(Locale.ROOT);
        rateLimiter.ensureAvailable(RateLimiter.Limit.LOGIN_FAILURES, limitKey);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
            );
        } catch (BadCredentialsException ex) {
            rateLimiter.record(RateLimiter.Limit.LOGIN_FAILURES, limitKey);
            throw ex;
        }

        UserPrincipal principal = Objects.requireNonNull((UserPrincipal) authentication.getPrincipal());
        String token = jwtService.generateToken(principal);

        return new LoginResponse(token, currentUserService.getMe(principal));
    }
}
