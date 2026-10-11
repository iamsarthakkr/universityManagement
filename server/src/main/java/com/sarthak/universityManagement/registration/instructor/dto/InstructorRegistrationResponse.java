package com.sarthak.universityManagement.registration.instructor.dto;

import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record InstructorRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    Instant createdAt,
    Instant reviewedAt,
    RegistrationStatus status,
    DepartmentResponse department,
    List<RegistrationAction> allowedActions
) { }
