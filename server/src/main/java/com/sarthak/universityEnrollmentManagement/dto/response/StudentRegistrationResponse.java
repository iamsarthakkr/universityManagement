package com.sarthak.universityEnrollmentManagement.dto.response;

import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StudentRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    LocalDate dateOfBirth,
    String address,
    String fatherName,
    String motherName,
    RegistrationStatus status
) {
}
