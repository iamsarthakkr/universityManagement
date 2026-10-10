package com.sarthak.universityManagement.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CourseRequest(

    @NotBlank(message = "code required")
    @Size(max = 10, message = "code must be between 1 and 10 characters")
    String code,
    
    @NotBlank(message = "title required")
    @Size(max = 100, message = "title must be between 1 and 100 characters")
    String title,
    
    @NotBlank(message = "description required")
    @Size(max = 255, message = "description must be between 1 and 255 characters")
    String description,
    
    @NotNull(message = "credits required")
    @Min(value = 1, message = "credits must be between 1 and 9")
    @Max(value = 9, message = "credits must be between 1 and 9")
    Integer credits,
    
    @NotNull(message = "department required")
    Integer departmentId
) {
}
