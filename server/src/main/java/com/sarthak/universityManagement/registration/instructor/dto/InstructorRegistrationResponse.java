package com.sarthak.universityManagement.registration.instructor.dto;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record InstructorRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    String department,
    LocalDate submittedAt,
    RegistrationStatus status
) {
}
