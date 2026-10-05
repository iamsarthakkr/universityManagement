package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.courseOffering.CourseOfferingMapper;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.student.StudentMapper;

import java.time.LocalDate;

public final class EnrollmentMapper {
    public static EnrollmentResponse toResponse(EnrollmentEntity enrollmentEntity, LocalDate today) {
        return EnrollmentResponse.builder()
            .id(enrollmentEntity.getId())
            .enrollmentStatus(enrollmentEntity.getStatus())
            .student(StudentMapper.toResponse(enrollmentEntity.getStudent()))
            .courseOffering(CourseOfferingMapper.toResponse(enrollmentEntity.getCourseOffering(), today))
            .build();
    }
}
