package com.sarthak.universityManagement.student.dto;

import jakarta.annotation.Nullable;

public record StudentFilter(
    @Nullable Integer departmentId
) { }
