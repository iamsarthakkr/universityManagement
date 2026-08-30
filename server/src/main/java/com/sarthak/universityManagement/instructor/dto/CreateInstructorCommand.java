package com.sarthak.universityManagement.instructor.dto;

import com.sarthak.universityManagement.department.DepartmentEntity;
import lombok.Builder;

@Builder
public record CreateInstructorCommand(
    String firstName,
    String lastName,
    String phoneNumber,
    DepartmentEntity department
) {
}
