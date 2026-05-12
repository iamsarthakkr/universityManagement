package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.student.dto.CreateStudentCommand;
import com.sarthak.universityManagement.user.dto.CreateUserCommand;
import com.sarthak.universityManagement.common.types.Role;

public class StudentRegistrationMapper {
    public static StudentRegistrationEntity toEntity(StudentRegistrationRequest request) {
        return StudentRegistrationEntity
            .builder()
            .username(request.username())
            .password(request.password())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .phoneNumber(request.phoneNumber())
            .dateOfBirth(request.dateOfBirth())
            .address(request.address())
            .fatherName(request.fatherName())
            .motherName(request.motherName())
            .build();
    }
    
    public static StudentRegistrationResponse toResponse(StudentRegistrationEntity entity) {
        return StudentRegistrationResponse
            .builder()
            .id(entity.getId())
            .username(entity.getUsername())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .email(entity.getEmail())
            .phoneNumber(entity.getPhoneNumber())
            .dateOfBirth(entity.getDateOfBirth())
            .address(entity.getAddress())
            .fatherName(entity.getFatherName())
            .motherName(entity.getMotherName())
            .status(entity.getRegistrationStatus())
            .build();
    }
    
    public static CreateUserCommand toCreateUserCommand(StudentRegistrationEntity entity) {
        return CreateUserCommand
            .builder()
            .username(entity.getUsername())
            .hashedPassword(entity.getPassword())
            .email(entity.getEmail())
            .role(Role.STUDENT)
            .build();
    }
    
    public static CreateStudentCommand toCreateStudentCommand(StudentRegistrationEntity entity) {
        return CreateStudentCommand
            .builder()
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .phoneNumber(entity.getPhoneNumber())
            .dateOfBirth(entity.getDateOfBirth())
            .address(entity.getAddress())
            .fatherName(entity.getFatherName())
            .motherName(entity.getMotherName())
            .build();
    }
    
}
