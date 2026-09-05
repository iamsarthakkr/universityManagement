package com.sarthak.universityManagement.instructor.dto;

import lombok.Builder;

@Builder
public record InstructorResponse(
        Integer id,
        String firstName,
        String lastName,
        Integer departmentId
) {
}
