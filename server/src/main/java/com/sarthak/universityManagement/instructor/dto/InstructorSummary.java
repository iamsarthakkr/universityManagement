package com.sarthak.universityManagement.instructor.dto;

import lombok.Builder;

@Builder
public record InstructorSummary(
    Integer id,
    String firstName,
    String lastName
) { }
