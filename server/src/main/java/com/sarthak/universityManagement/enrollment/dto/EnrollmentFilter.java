package com.sarthak.universityManagement.enrollment.dto;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import jakarta.annotation.Nullable;

public record EnrollmentFilter(
    @Nullable Integer semesterId,
    @Nullable EnrollmentStatus status
) { }
