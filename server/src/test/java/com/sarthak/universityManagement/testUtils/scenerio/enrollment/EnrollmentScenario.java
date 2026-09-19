package com.sarthak.universityManagement.testUtils.scenerio.enrollment;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.student.StudentEntity;

public record EnrollmentScenario(
    EnrollmentEntity enrollment,
    StudentEntity student,
    CourseOfferingEntity courseOffering,
    CourseEntity course,
    InstructorEntity instructor,
    DepartmentEntity department,
    SemesterEntity semester
) {}
