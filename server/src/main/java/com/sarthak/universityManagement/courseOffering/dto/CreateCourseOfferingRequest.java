package com.sarthak.universityManagement.courseOffering.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseOfferingRequest(
    @NotNull(message = "course id required")
    Integer courseId,

    @NotNull(message = "instructor id required")
    Integer instructorId,

    @NotNull(message = "semester id required")
    Integer semesterId,

    @NotBlank(message = "section required")
    String section,

    @NotNull(message = "capacity required")
    @Min(value = 0, message = "capacity has to be positive")
    Integer capacity
) {}
