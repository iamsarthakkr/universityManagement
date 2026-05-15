package com.sarthak.universityManagement.registration.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRegistrationRequest(
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
    @NotBlank(message = "phone number required")
    @Pattern(regexp = "^[0-9]{10}$", message = "invalid phone number")
    String phoneNumber,
    @NotNull(message = "date of birth required")
    LocalDate dateOfBirth,
    @NotBlank(message = "address required")
    String address,
    @NotBlank(message = "father's name required")
    String fatherName,
    @NotBlank(message = "mother's name required")
    String motherName
) {
}
