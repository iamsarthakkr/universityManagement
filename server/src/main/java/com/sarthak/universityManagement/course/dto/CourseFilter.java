package com.sarthak.universityManagement.course.dto;

import jakarta.annotation.Nullable;

public record CourseFilter(
    @Nullable Integer departmentId
) { }
