package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.courseOffering.CourseOfferingMapper;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.student.StudentMapper;

import java.time.LocalDate;
import java.util.List;

public final class EnrollmentMapper {
    public static EnrollmentResponse toResponse(EnrollmentEntity enrollmentEntity, LocalDate today, List<EnrollmentAction> allowedActions) {
        return EnrollmentResponse.builder()
            .id(enrollmentEntity.getId())
            .status(enrollmentEntity.getStatus())
            .student(StudentMapper.toSummary(enrollmentEntity.getStudent()))
            .courseOffering(CourseOfferingMapper.toSummary(enrollmentEntity.getCourseOffering(), today))
            .allowedActions(allowedActions)
            .createdAt(enrollmentEntity.getCreatedAt())
            .updatedAt(enrollmentEntity.getUpdatedAt())
            .build();
    }
}
