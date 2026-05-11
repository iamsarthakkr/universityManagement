package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.dto.request.InstructorRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.InstructorResponse;
import com.sarthak.universityEnrollmentManagement.entity.InstructorEntity;

public class InstructorMapper {
    public static InstructorEntity toEntity(InstructorRequest instructorRequest) {
        return InstructorEntity
            .builder()
            .firstName(instructorRequest.getFirstName())
            .lastName(instructorRequest.getLastName())
            .email(instructorRequest.getEmail())
            .phoneNumber(instructorRequest.getPhoneNumber())
            .department(instructorRequest.getDepartment())
            .build();
    }
    
    public static InstructorResponse toResponse(InstructorEntity instructor) {
        return InstructorResponse
            .builder()
            .id(instructor.getId())
            .firstName(instructor.getFirstName())
            .lastName(instructor.getLastName())
            .email(instructor.getEmail())
            .phoneNumber(instructor.getPhoneNumber())
            .department(instructor.getDepartment())
            .build();
    }
}
