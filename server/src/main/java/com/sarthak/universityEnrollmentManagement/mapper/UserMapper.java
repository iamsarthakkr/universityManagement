package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.dto.internal.CreateUserCommand;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;

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
