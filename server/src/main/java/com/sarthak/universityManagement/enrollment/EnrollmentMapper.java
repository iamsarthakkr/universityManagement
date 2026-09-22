package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.courseOffering.CourseOfferingMapper;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentDetailResponse;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.student.StudentMapper;

public final class EnrollmentMapper {
    public static EnrollmentResponse toResponse(EnrollmentEntity enrollmentEntity) {
        return EnrollmentResponse.builder()
            .id(enrollmentEntity.getId())
            .studentId(enrollmentEntity.getStudent().getId())
            .courseOfferingId(enrollmentEntity.getCourseOffering().getId())
            .enrollmentStatus(enrollmentEntity.getStatus())
            .build();
    }

    public static EnrollmentDetailResponse toDetailedResponse(EnrollmentEntity enrollmentEntity) {
        return EnrollmentDetailResponse.builder()
            .id(enrollmentEntity.getId())
            .enrollmentStatus(enrollmentEntity.getStatus())
            .student(StudentMapper.toResponse(enrollmentEntity.getStudent()))
            .courseOffering(CourseOfferingMapper.toResponse(enrollmentEntity.getCourseOffering()))
            .build();
    }
}
