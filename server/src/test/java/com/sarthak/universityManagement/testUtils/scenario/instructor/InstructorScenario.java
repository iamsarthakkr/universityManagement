package com.sarthak.universityManagement.testUtils.scenario.instructor;

import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;

public record InstructorScenario(
    InstructorEntity instructor,
    DepartmentEntity department
) {}
