package com.sarthak.universityManagement.instructor.validators;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;

public class InstructorValidator {

    public static void validateInstructorForCourse(CourseEntity course, InstructorEntity instructor) {
        var courseDepartment = course.getDepartment().getCode();
        var instructorDepartment = instructor.getDepartment().getCode();
        if(!courseDepartment.equals(instructorDepartment)) {
            throw new BadRequestException("instructor and course belong to different departments");
        }
    }

}
