package com.sarthak.universityManagement.student.dto;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

@Builder
public record StudentResponse(
    Integer id,
    String firstName,
    String lastName,
    DepartmentResponse department
) { }
