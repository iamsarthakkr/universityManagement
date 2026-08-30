package com.sarthak.universityManagement.registration.instructor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InstructorRegistrationRequest(
    @NotBlank(message = "username required")
    String username,

    @NotBlank(message = "password required")
    @Size(min = 8, max = 50, message = "password must be between 8 and 50 characters")
    String password,

    @NotBlank(message = "first name required")
    String firstName,

    String lastName,

    @NotBlank(message = "email required")
    @Email(message = "invalid email")
    String email,

    @NotNull(message = "department id required")
    Integer departmentId
) {
}
