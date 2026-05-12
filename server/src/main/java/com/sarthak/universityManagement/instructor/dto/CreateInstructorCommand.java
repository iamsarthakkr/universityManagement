package com.sarthak.universityManagement.instructor.dto;

import lombok.Builder;

@Builder
public record CreateInstructorCommand(
    String firstName,
    String lastName,
    String phoneNumber,
    String department
) {
}
