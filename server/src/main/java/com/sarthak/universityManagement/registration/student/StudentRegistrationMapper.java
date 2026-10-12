package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.department.DepartmentMapper;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.student.dto.CreateStudentCommand;
import com.sarthak.universityManagement.user.dto.CreateUserCommand;
import com.sarthak.universityManagement.common.types.Role;

import java.util.List;

public class StudentRegistrationMapper {
    public static StudentRegistrationEntity toEntity(StudentRegistrationRequest request) {
        return StudentRegistrationEntity
            .builder()
            .username(request.username())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .dateOfBirth(request.dateOfBirth())
            .build();
    }
    
    public static StudentRegistrationResponse toResponse(StudentRegistrationEntity studentRegistration, List<RegistrationAction> allowedActions) {
        return StudentRegistrationResponse
            .builder()
            .id(studentRegistration.getId())
            .username(studentRegistration.getUsername())
            .firstName(studentRegistration.getFirstName())
            .lastName(studentRegistration.getLastName())
            .email(studentRegistration.getEmail())
            .status(studentRegistration.getRegistrationStatus())
            .dateOfBirth(studentRegistration.getDateOfBirth())
            .createdAt(studentRegistration.getCreatedAt())
            .reviewedAt(studentRegistration.getReviewedAt())
            .department(DepartmentMapper.toResponse(studentRegistration.getDepartment()))
            .allowedActions(allowedActions)
            .build();
    }
    
    public static CreateUserCommand toCreateUserCommand(StudentRegistrationEntity studentRegistration) {
        return CreateUserCommand
            .builder()
            .username(studentRegistration.getUsername())
            .hashedPassword(studentRegistration.getPassword())
            .email(studentRegistration.getEmail())
            .role(Role.STUDENT)
            .build();
    }
    
    public static CreateStudentCommand toCreateStudentCommand(StudentRegistrationEntity studentRegistration) {
        return CreateStudentCommand
            .builder()
            .firstName(studentRegistration.getFirstName())
            .lastName(studentRegistration.getLastName())
            .dateOfBirth(studentRegistration.getDateOfBirth())
            .department(studentRegistration.getDepartment())
            .build();
    }
    
}
