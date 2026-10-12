package com.sarthak.universityManagement.courseOffering.dto;

import jakarta.annotation.Nullable;

public record CourseOfferingFilter(
    @Nullable Integer departmentId,
    @Nullable Integer semesterId,
    @Nullable Integer instructorId
) { }
