package com.sarthak.universityManagement.testUtils.security;

import com.sarthak.universityManagement.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        var principal = UserPrincipal.builder()
            .username(annotation.username())
            .role(annotation.role())
            .password("test-password")
            .userId(1)
            .enabled(true)
            .build();

        var authentication =
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
            );

        context.setAuthentication(authentication);

        return context;
    }
}