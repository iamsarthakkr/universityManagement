package com.sarthak.universityManagement.registration.instructor.dto;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import lombok.Builder;

@Builder
public record InstructorRegistrationResponse(
    Integer id,
    String username,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String department,
    RegistrationStatus status
) {
}
