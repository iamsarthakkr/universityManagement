package com.sarthak.universityManagement.course.dto;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

@Builder
public record CourseResponse(
    Integer id,
    DepartmentResponse department,
    String code,
    String title,
    String description,
    Integer credits
) {
}
