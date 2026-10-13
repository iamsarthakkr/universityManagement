package com.sarthak.universityManagement.registration.student.dto;

import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record StudentRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDate dateOfBirth,
    Instant createdAt,
    Instant reviewedAt,
    RegistrationStatus status,
    DepartmentResponse department,
    List<RegistrationAction> allowedActions
) { }
