package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.enrollment.EnrollmentEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

@TestComponent
@Profile("test")
public class EnrollmentSeeder {
    @Autowired
    private EnrollmentRepo enrollmentRepo;

    public EnrollmentEntity save(EnrollmentEntity enrollmentEntity) {
        return enrollmentRepo.saveAndFlush(enrollmentEntity);
    }

    public EnrollmentEntity seedOrGet(EnrollmentEntity.EnrollmentEntityBuilder builder) {
        var toSave = builder.build();

        var studentId = toSave.getStudent().getId();
        var offeringId = toSave.getCourseOffering().getId();


        return enrollmentRepo
            .findByStudentIdAndCourseOfferingId(studentId,  offeringId)
            .orElseGet(() -> enrollmentRepo.saveAndFlush(toSave));
    }

}
