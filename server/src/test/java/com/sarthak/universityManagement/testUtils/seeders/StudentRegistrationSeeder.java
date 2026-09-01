package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public final class StudentRegistrationSeeder {
    private final StudentRegistrationRepo studentRegistrationRepo;

    @Autowired
    public StudentRegistrationSeeder(StudentRegistrationRepo studentRegistrationRepo) {
        this.studentRegistrationRepo = studentRegistrationRepo;
    }

    public StudentRegistrationEntity saveStudentRegistration(StudentRegistrationEntity studentRegistrationEntity) {
        return studentRegistrationRepo.saveAndFlush(studentRegistrationEntity);
    }

    public StudentRegistrationEntity saveDefaultStudentRegistration() {
        return saveStudentRegistration(
                StudentRegistrationFixtures
                        .studentRegistration()
                        .build()
        );
    }
}
