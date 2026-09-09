package com.sarthak.universityManagement.courseOffering.validators;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.types.SemesterStatus;

public final class CourseOfferingValidator {

    public static void validateSemesterAllowOfferings(SemesterEntity semester) {
        var status = semester.getStatus();
        if( status == SemesterStatus.COMPLETED || status == SemesterStatus.CANCELLED) {
            throw new BadRequestException("Semester doesn't allow offerings");
        }
    }

    public static void validateInstructorForCourse(CourseEntity course, InstructorEntity instructor) {
        var courseDepartment = course.getDepartment().getCode();
        var instructorDepartment = instructor.getDepartment().getCode();
        if(!courseDepartment.equals(instructorDepartment)) {
            throw new BadRequestException("instructor and course belong to different departments");
        }
    }

}
