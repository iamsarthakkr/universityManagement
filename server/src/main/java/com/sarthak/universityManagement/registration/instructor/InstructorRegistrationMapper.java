package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.instructor.dto.CreateInstructorCommand;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.user.dto.CreateUserCommand;
import com.sarthak.universityManagement.common.types.Role;

public class InstructorRegistrationMapper {
    public static InstructorRegistrationEntity toEntity(InstructorRegistrationRequest request) {
        return InstructorRegistrationEntity
            .builder()
            .username(request.username())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
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
            .department(entity.getDepartment())
            .build();
    }
    
}
