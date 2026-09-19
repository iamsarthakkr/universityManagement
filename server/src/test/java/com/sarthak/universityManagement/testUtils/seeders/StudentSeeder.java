package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.testUtils.fixtures.StudentFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
@Import(UserSeeder.class)
public final class StudentSeeder {
    private final StudentRepo studentRepo;
    private final UserSeeder userSeeder;

    @Autowired
    public StudentSeeder(
        StudentRepo studentRepo,
        UserSeeder userSeeder
    ) {
        this.studentRepo = studentRepo;
        this.userSeeder = userSeeder;
    }

    public StudentEntity save(StudentEntity studentEntity) {
        return studentRepo.saveAndFlush(studentEntity);
    }

    public StudentEntity saveDefault(DepartmentEntity department) {
        var defaultUser = userSeeder.saveDefault(Role.INSTRUCTOR);
        return save(
            StudentFixtures
                .student(department)
                .user(defaultUser)
                .build()
        );
    }

    public StudentEntity seedOrGet(DepartmentEntity department, String username) {
        var existing = studentRepo.findByUser_Username(username);

        if(existing.isPresent()) {
            return existing.get();
        }

        var user = userSeeder.seedOrGet(Role.STUDENT, username);
        return save(
            StudentFixtures.student(department)
                .user(user)
                .build()
        );
    }

}
