package com.sarthak.universityManagement.course.dto;

import lombok.Builder;

@Builder
public record CourseSummary(
    Integer id,
    String code,
    String title,
    Integer credits
) { }
