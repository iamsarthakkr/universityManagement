package com.sarthak.universityManagement.enrollment.dto;

import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.student.dto.StudentResponse;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record EnrollmentResponse(
    Integer id,
    EnrollmentStatus enrollmentStatus,
    StudentResponse student,
    CourseOfferingResponse courseOffering,
    List<EnrollmentAction> allowedActions,
    Instant createdAt,
    Instant updatedAt
) {}
