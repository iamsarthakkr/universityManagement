package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.courseOffering.CourseOfferingMapper;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.student.StudentMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public final class EnrollmentMapper {
    public static EnrollmentResponse toResponse(EnrollmentEntity enrollmentEntity, LocalDate today, List<EnrollmentAction> allowedActions) {
        return EnrollmentResponse.builder()
            .id(enrollmentEntity.getId())
            .enrollmentStatus(enrollmentEntity.getStatus())
            .student(StudentMapper.toResponse(enrollmentEntity.getStudent()))
            .courseOffering(CourseOfferingMapper.toResponse(enrollmentEntity.getCourseOffering(), today))
            .allowedActions(allowedActions)
            .createdAt(enrollmentEntity.getCreatedAt())
            .updatedAt(enrollmentEntity.getUpdatedAt())
            .build();
    }
}
