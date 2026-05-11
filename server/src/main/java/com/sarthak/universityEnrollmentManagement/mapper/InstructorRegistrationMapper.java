package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.dto.request.InstructorRegistrationRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.InstructorRegistrationResponse;
import com.sarthak.universityEnrollmentManagement.entity.InstructorRegistrationEntity;

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
}
