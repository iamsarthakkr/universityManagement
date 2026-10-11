package com.sarthak.universityManagement.instructor.dto;

import jakarta.annotation.Nullable;

public record InstructorFilter(
    @Nullable Integer departmentId
) { }
