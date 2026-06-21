package com.sarthak.universityManagement.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(
    @NotBlank(message = "department required")
    String department,
    
    @NotBlank(message = "code required")
    String code,
    
    @NotBlank(message = "title required")
    String title,
    
    @NotBlank(message = "description required")
    String description,
    
    @NotNull(message = "credits required")
    @Min(value = 1, message = "credits must be between 1 and 10")
    @Max(value = 10, message = "credits must be between 1 and 10")
    Integer credits,
    
    @NotNull(message = "course capacity required")
    @Min(value = 1, message = "course capacity has to be positive")
    Integer capacity,
    
    @NotNull(message = "Instructor required")
    Integer instructorId
) {
}
