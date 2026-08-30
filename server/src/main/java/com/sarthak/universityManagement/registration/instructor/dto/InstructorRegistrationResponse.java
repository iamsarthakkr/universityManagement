package com.sarthak.universityManagement.registration.instructor.dto;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InstructorRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDate submittedAt,
    RegistrationStatus status,
    DepartmentResponse department
) {
}
