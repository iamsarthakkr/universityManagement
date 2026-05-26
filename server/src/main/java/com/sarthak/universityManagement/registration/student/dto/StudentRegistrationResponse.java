package com.sarthak.universityManagement.registration.student.dto;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StudentRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDate dateOfBirth,
    RegistrationStatus status
) {
}
