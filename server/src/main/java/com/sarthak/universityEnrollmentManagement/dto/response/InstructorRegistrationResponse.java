package com.sarthak.universityEnrollmentManagement.dto.response;

import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
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
