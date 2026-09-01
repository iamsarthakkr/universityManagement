package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorRegistrationFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public final class InstructorRegistrationSeeder {
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final DepartmentSeeder departmentSeeder;

    @Autowired
    public InstructorRegistrationSeeder(
            InstructorRegistrationRepo instructorRegistrationRepo,
            DepartmentSeeder departmentSeeder
    ) {
        this.instructorRegistrationRepo = instructorRegistrationRepo;
        this.departmentSeeder = departmentSeeder;
    }
    public InstructorRegistrationEntity saveInstructorRegistration(InstructorRegistrationEntity instructorRegistrationEntity) {
        return instructorRegistrationRepo.saveAndFlush(instructorRegistrationEntity);
    }

    public InstructorRegistrationEntity saveDefaultInstructorRegistration(String departmentCode) {
        var department = departmentSeeder.saveDefault(departmentCode);
        return saveInstructorRegistration(
                InstructorRegistrationFixtures
                        .instructorRegistration(department)
                        .build()
        );
    }
}
