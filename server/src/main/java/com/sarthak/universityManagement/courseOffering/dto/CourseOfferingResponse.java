package com.sarthak.universityManagement.courseOffering.dto;

import lombok.Builder;

@Builder
public record CourseOfferingResponse(
    int id,
    int courseId,
    int instructorId,
    int semesterId,
    int capacity,
    String section
) {}
