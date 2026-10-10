package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CurrentUserServiceTests {
    private static final int USER_ID = 42;

    private final CurrentUserService currentUserService = new CurrentUserService(null, null, null);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate(Authentication authentication) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private static UserPrincipal principal() {
        return UserPrincipal.builder()
            .userId(USER_ID)
            .username("current-user")
            .role(Role.INSTRUCTOR)
            .enabled(true)
            .build();
    }

    static Stream<Arguments> unauthenticatedCases() {
        return Stream.of(
            Arguments.of("no authentication", null),
            Arguments.of("anonymous user", new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))),
            Arguments.of("non-UserPrincipal principal", UsernamePasswordAuthenticationToken.authenticated(
                "some-user", null, AuthorityUtils.createAuthorityList("ROLE_ADMIN"))),
            Arguments.of("unauthenticated token", UsernamePasswordAuthenticationToken.unauthenticated(
                principal(), null))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unauthenticatedCases")
    void shouldRejectWhenNoUserPrincipal(String label, Authentication authentication) {
        authenticate(authentication);

        assertThrows(AuthenticationCredentialsNotFoundException.class, currentUserService::getCurrentUserPrincipal);
        assertThrows(AuthenticationCredentialsNotFoundException.class, currentUserService::getCurrentUserId);
        assertThrows(AuthenticationCredentialsNotFoundException.class, currentUserService::getCurrentUserRole);
    }

    @Test
    void shouldReturnUserPrincipalAndItsDetails() {
        var principal = principal();
        authenticate(UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));

        assertSame(principal, currentUserService.getCurrentUserPrincipal());
        assertEquals(USER_ID, currentUserService.getCurrentUserId());
        assertEquals(Role.INSTRUCTOR, currentUserService.getCurrentUserRole());
    }
}
