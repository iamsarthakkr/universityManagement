package com.sarthak.universityManagement.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CourseRequest(

    @NotBlank(message = "code required")
    @Max(value = 10, message = "code must be between 1 and 10 characters")
    String code,
    
    @NotBlank(message = "title required")
    @Max(value = 100, message = "code must be between 1 and 100 characters")
    String title,
    
    @NotBlank(message = "description required")
    @Max(value = 100, message = "code must be between 1 and 255 characters")
    String description,
    
    @NotNull(message = "credits required")
    @Min(value = 1, message = "credits must be between 1 and 10")
    @Max(value = 9, message = "credits must be between 1 and 9")
    Integer credits,
    
    @NotNull(message = "department required")
    Integer departmentId
) {
}
