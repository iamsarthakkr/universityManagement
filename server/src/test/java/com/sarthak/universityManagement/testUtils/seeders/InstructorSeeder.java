package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
@Import(UserSeeder.class)
public final class InstructorSeeder {
    private final InstructorRepo instructorRepo;
    private final UserSeeder userSeeder;

    @Autowired
    public InstructorSeeder(
        InstructorRepo instructorRepo,
        UserSeeder userSeeder
    ) {
        this.instructorRepo = instructorRepo;
        this.userSeeder = userSeeder;
    }

    public InstructorEntity saveInstructor(InstructorEntity instructorEntity) {
        return instructorRepo.saveAndFlush(instructorEntity);
    }

    public InstructorEntity saveDefaultInstructor(DepartmentEntity department) {
        var defaultUser = userSeeder.saveDefault(Role.INSTRUCTOR);
        return saveInstructor(
            InstructorFixtures
                .instructor()
                .user(defaultUser)
                .department(department)
                .build()
        );
    }

}
