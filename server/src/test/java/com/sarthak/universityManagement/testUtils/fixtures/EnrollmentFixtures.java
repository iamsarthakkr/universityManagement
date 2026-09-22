package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentEntity;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.student.StudentEntity;

public class EnrollmentFixtures {
    public static EnrollmentEntity.EnrollmentEntityBuilder enrollment(
        StudentEntity student,
        CourseOfferingEntity courseOffering
    ) {
        return EnrollmentEntity.builder()
            .student(student)
            .courseOffering(courseOffering)
            .status(EnrollmentStatus.PENDING);
    }
}
