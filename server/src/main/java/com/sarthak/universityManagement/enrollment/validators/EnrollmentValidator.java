package com.sarthak.universityManagement.enrollment.validators;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import com.sarthak.universityManagement.student.StudentEntity;

import java.time.LocalDate;

public final class EnrollmentValidator {

    public static void validateUniqueEnrollment(EnrollmentRepo enrollmentRepo, StudentEntity student, CourseOfferingEntity courseOffering) {
        if(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student.getId(), courseOffering.getId())) {
            throw new BadRequestException("Enrollment already exists for student in the course offering");
        }
    }

    public static void validateOffering(CourseOfferingEntity courseOffering) {
        var semester = courseOffering.getSemester();
        if(!semester.isRegistrationOpen(LocalDate.now())) {
            throw new BadRequestException("Registration is not open");
        }
    }

}
