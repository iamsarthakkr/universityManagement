package com.sarthak.universityManagement.testUtils.scenerio.student;

import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.student.StudentEntity;

public record StudentScenario(
    StudentEntity student,
    DepartmentEntity department
) {}
