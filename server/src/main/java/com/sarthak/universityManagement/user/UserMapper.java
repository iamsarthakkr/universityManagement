package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.user.dto.CreateUserCommand;
import com.sarthak.universityManagement.user.dto.MeResponse;
import com.sarthak.universityManagement.user.dto.Profile;
import jakarta.annotation.Nullable;

public class UserMapper {
    public static UserEntity toEntity(CreateUserCommand createUserCommand) {
        return UserEntity
            .builder()
            .username(createUserCommand.username())
            .password(createUserCommand.hashedPassword())
            .email(createUserCommand.email())
            .role(createUserCommand.role())
            .build();
    }

    public static MeResponse toMe(UserPrincipal user, @Nullable Profile profileOrNull) {
        return MeResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .profile(profileOrNull)
            .build();
    }

}
