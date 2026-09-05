package com.sarthak.universityManagement.course.dto;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

@Builder
public record CourseResponse(
    Integer courseId,
    Integer departmentId,
    String departmentName,
    String code,
    String title,
    String description,
    Integer credits,
    Integer capacity,
    Integer instructorId,
    String instructor
) {
}
