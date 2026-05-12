package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.user.dto.CreateUserCommand;

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
}
