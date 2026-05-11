package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.Role;

public class UserMapper {
    public static UserEntity toEntityFromStudentRegistration(StudentRegistrationEntity entity) {
        return UserEntity
            .builder()
            .username(entity.getUsername())
            .password(entity.getPassword())
            .email(entity.getEmail())
            .role(Role.STUDENT)
            .build();
    }
    
}
