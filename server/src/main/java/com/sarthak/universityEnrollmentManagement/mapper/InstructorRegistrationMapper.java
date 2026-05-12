package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.dto.internal.CreateInstructorCommand;
import com.sarthak.universityEnrollmentManagement.dto.internal.CreateStudentCommand;
import com.sarthak.universityEnrollmentManagement.dto.internal.CreateUserCommand;
import com.sarthak.universityEnrollmentManagement.dto.request.InstructorRegistrationRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.InstructorRegistrationResponse;
import com.sarthak.universityEnrollmentManagement.entity.InstructorRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.Role;

public class InstructorRegistrationMapper {
    public static InstructorRegistrationEntity toEntity(InstructorRegistrationRequest request) {
        return InstructorRegistrationEntity
            .builder()
            .username(request.username())
            .password(request.password())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .phoneNumber(request.phoneNumber())
            .department(request.department())
            .build();
    }
    
    public static InstructorRegistrationResponse toResponse(InstructorRegistrationEntity entity) {
        return InstructorRegistrationResponse
            .builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .email(entity.getEmail())
            .phoneNumber(entity.getPhoneNumber())
            .department(entity.getDepartment())
            .status(entity.getRegistrationStatus())
            .build();
    }
    
    public static CreateUserCommand toCreateUserCommand(InstructorRegistrationEntity entity) {
        return CreateUserCommand
            .builder()
            .username(entity.getUsername())
            .hashedPassword(entity.getPassword())
            .email(entity.getEmail())
            .role(Role.INSTRUCTOR)
            .build();
    }
    
    
    public static CreateInstructorCommand toCreateInstructorCommand(InstructorRegistrationEntity entity) {
        return CreateInstructorCommand
            .builder()
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .phoneNumber(entity.getPhoneNumber())
            .department(entity.getDepartment())
            .build();
    }
    
}
