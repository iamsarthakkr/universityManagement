package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.dto.request.StudentRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.StudentResponse;
import com.sarthak.universityEnrollmentManagement.entity.StudentEntity;

public class StudentMapper {
    public static StudentEntity toEntity(StudentRequest studentRequest) {
        return StudentEntity.builder()
            .firstName(studentRequest.getFirstName())
            .lastName((studentRequest.getLastName()))
            .email(studentRequest.getEmail())
            .build();
    }
    
    public static StudentResponse toResponse(StudentEntity student) {
        return StudentResponse.builder()
            .id(student.getId())
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .email(student.getEmail())
            .build();
    }
}
