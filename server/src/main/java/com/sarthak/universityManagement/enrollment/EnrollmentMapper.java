package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;

public final class EnrollmentMapper {
    public static EnrollmentResponse toResponse(EnrollmentEntity enrollmentEntity) {
        return EnrollmentResponse.builder()
            .id(enrollmentEntity.getId())
            .studentId(enrollmentEntity.getStudent().getId())
            .courseOfferingId(enrollmentEntity.getCourseOffering().getId())
            .enrollmentStatus(enrollmentEntity.getStatus())
            .build();
    }
}
