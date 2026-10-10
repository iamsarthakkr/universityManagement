package com.sarthak.universityManagement.testUtils.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;

// What a @PreAuthorize-guarded service call does for a given actor
public enum AuthOutcome {
    ALLOWED,
    ACCESS_DENIED,
    UNAUTHENTICATED;

    public Class<? extends Exception> expectedException() {
        return switch (this) {
            case ACCESS_DENIED -> AuthorizationDeniedException.class;
            case UNAUTHENTICATED -> AuthenticationCredentialsNotFoundException.class;
            case ALLOWED -> throw new IllegalStateException("ALLOWED has no exception");
        };
    }
}
