package com.sarthak.universityManagement.testUtils.scenerio.courseOffering;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.semester.SemesterEntity;

public record CourseOfferingScenario(
    CourseOfferingEntity courseOffering,
    CourseEntity course,
    SemesterEntity semester,
    InstructorEntity instructor,
    DepartmentEntity department
) { }
