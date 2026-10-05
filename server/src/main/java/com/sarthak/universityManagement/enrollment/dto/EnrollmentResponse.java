package com.sarthak.universityManagement.enrollment.dto;

import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.student.dto.StudentResponse;
import lombok.Builder;

@Builder
public record EnrollmentResponse(
    Integer id,
    EnrollmentStatus enrollmentStatus,
    StudentResponse student,
    CourseOfferingResponse courseOffering
) {}
