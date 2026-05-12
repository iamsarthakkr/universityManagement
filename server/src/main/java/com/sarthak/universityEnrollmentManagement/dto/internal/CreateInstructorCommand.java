package com.sarthak.universityEnrollmentManagement.dto.internal;

import lombok.Builder;

@Builder
public record CreateInstructorCommand(
    String firstName,
    String lastName,
    String phoneNumber,
    String department
) {
}
