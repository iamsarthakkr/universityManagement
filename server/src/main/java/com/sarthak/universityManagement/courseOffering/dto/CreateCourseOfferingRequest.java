package com.sarthak.universityManagement.courseOffering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateCourseOfferingRequest(
    @NotNull(message = "course id required")
    Integer courseId,

    @NotNull(message = "instructor id required")
    Integer instructorId,

    @NotNull(message = "semester id required")
    Integer semesterId,

    @NotBlank(message = "section required")
    @Size(max = 10, message = "section must be between 1 and 10 characters long")
    String section,

    @NotNull(message = "capacity required")
    @Min(value = 1, message = "capacity has to be positive")
    Integer capacity
) {}
