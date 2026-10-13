package com.sarthak.universityManagement.user.dto;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import lombok.Builder;

@Builder
public record Profile(
    Integer id,
    String firstName,
    String lastName,
    DepartmentResponse department
) {}
