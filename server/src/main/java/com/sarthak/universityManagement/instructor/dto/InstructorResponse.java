package com.sarthak.universityManagement.instructor.dto;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

@Builder
public record InstructorResponse(
        Integer id,
        String firstName,
        String lastName,
        DepartmentResponse department
) {
}
