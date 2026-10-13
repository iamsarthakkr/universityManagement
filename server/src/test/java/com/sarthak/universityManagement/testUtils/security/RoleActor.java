package com.sarthak.universityManagement.testUtils.security;

import com.sarthak.universityManagement.common.types.Role;

// Actors for checks that depend only on the role; relationship-based tests (enrollment) define their own
public enum RoleActor {
    ADMIN,
    INSTRUCTOR,
    STUDENT,
    ANONYMOUS;

    public void authenticate() {
        switch (this) {
            case ADMIN -> TestAuthentication.asRole(Role.ADMIN);
            case INSTRUCTOR -> TestAuthentication.asRole(Role.INSTRUCTOR);
            case STUDENT -> TestAuthentication.asRole(Role.STUDENT);
            case ANONYMOUS -> TestAuthentication.clear();
        }
    }
}
