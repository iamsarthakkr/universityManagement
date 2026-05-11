package com.sarthak.universityEnrollmentManagement.dto.request;

public record InstructorRegistrationRequest(
    String username,
    String password,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String department
) {
}
