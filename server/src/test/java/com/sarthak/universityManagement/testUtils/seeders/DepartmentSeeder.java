package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.department.DepartmentRepo;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public final class DepartmentSeeder {
    private final DepartmentRepo departmentRepo;
    private final DepartmentEntity.DepartmentEntityBuilder departmentEntityBuilder = DepartmentFixtures.departmentWithCode();

    @Autowired
    public DepartmentSeeder(DepartmentRepo departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    public DepartmentEntity save(DepartmentEntity departmentEntity) {
        return departmentRepo.saveAndFlush(departmentEntity);
    }

    public DepartmentEntity saveDefault(String code) {
        return departmentRepo.saveAndFlush(
                departmentEntityBuilder
                        .code(code)
                        .build()
        );
    }
}
