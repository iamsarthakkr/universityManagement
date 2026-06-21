package com.sarthak.universityManagement.course.dto;

import lombok.Builder;

@Builder
public record CourseResponse(
    Integer courseId,
    String department,
    String code,
    String title,
    String description,
    Integer credits,
    Integer capacity,
    Integer instructorId,
    String instructor
) {
}
