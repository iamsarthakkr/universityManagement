package com.sarthak.universityManagement.registration.instructor.dto;

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
