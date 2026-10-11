package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.department.DepartmentMapper;
import com.sarthak.universityManagement.instructor.dto.CreateInstructorCommand;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.user.dto.CreateUserCommand;
import com.sarthak.universityManagement.common.types.Role;

import java.util.List;

public class InstructorRegistrationMapper {
    public static InstructorRegistrationEntity toEntity(InstructorRegistrationRequest request) {
        return InstructorRegistrationEntity
            .builder()
            .username(request.username())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .build();
    }
    
    public static InstructorRegistrationResponse toResponse(InstructorRegistrationEntity instructorRegistration, List<RegistrationAction> allowedActions) {
        return InstructorRegistrationResponse
            .builder()
            .id(instructorRegistration.getId())
            .username(instructorRegistration.getUsername())
            .firstName(instructorRegistration.getFirstName())
            .lastName(instructorRegistration.getLastName())
            .email(instructorRegistration.getEmail())
            .department(DepartmentMapper.toResponse(instructorRegistration.getDepartment()))
            .createdAt(instructorRegistration.getCreatedAt())
            .reviewedAt(instructorRegistration.getReviewedAt())
            .status(instructorRegistration.getRegistrationStatus())
            .allowedActions(allowedActions)
            .build();
    }
    
    public static CreateUserCommand toCreateUserCommand(InstructorRegistrationEntity instructorRegistration) {
        return CreateUserCommand
            .builder()
            .username(instructorRegistration.getUsername())
            .hashedPassword(instructorRegistration.getPassword())
            .email(instructorRegistration.getEmail())
            .role(Role.INSTRUCTOR)
            .build();
    }

    public static CreateInstructorCommand toCreateInstructorCommand(InstructorRegistrationEntity instructorRegistration) {
        return CreateInstructorCommand
            .builder()
            .firstName(instructorRegistration.getFirstName())
            .lastName(instructorRegistration.getLastName())
            .department(instructorRegistration.getDepartment())
            .build();
    }
    
}
