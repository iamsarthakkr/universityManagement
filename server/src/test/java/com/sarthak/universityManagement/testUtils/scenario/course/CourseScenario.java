package com.sarthak.universityManagement.testUtils.scenario.course;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.department.DepartmentEntity;

public record CourseScenario(
    DepartmentEntity department,
    CourseEntity course
) {}
