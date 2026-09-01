package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.user.UserEntity;

public final class UserFixtures {

    public static UserEntity.UserEntityBuilder user() {
        return UserEntity.builder()
                .username("test-user")
                .password("test-password")
                .email("test@test.com")
                .role(Role.ADMIN)
                .active(true);
    }

    public static UserPrincipal.UserPrincipalBuilder userPrincipal() {
        return UserPrincipal.builder()
                .username("test-user")
                .password("test-password")
                .role(Role.ADMIN)
                .enabled(true);
    }
}
