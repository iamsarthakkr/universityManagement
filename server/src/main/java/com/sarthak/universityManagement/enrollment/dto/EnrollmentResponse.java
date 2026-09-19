package com.sarthak.universityManagement.enrollment.dto;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import lombok.Builder;

@Builder
public record EnrollmentResponse(
    Integer id,
    Integer studentId,
    Integer courseOfferingId,
    EnrollmentStatus enrollmentStatus
) {
}
