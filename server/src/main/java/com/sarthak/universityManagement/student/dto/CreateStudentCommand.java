package com.sarthak.universityManagement.student.dto;

import com.sarthak.universityManagement.department.DepartmentEntity;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateStudentCommand(
    String firstName,
    String lastName,
    String phoneNumber,
    LocalDate dateOfBirth,
    String address,
    String fatherName,
    String motherName,
    DepartmentEntity department
) {
}
