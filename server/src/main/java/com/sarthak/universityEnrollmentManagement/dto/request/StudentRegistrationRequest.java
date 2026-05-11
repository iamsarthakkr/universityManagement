package com.sarthak.universityEnrollmentManagement.dto.request;

import java.time.LocalDate;

public record StudentRegistrationRequest(
    String username,
    String password,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    LocalDate dateOfBirth,
    String address,
    String fatherName,
    String motherName
) {
}
