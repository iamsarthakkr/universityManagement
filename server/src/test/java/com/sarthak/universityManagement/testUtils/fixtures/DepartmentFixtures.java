package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.department.DepartmentEntity;

public final class DepartmentFixtures {

    public static DepartmentEntity.DepartmentEntityBuilder departmentWithCode() {
        return DepartmentEntity.builder()
                .name("Department 1")
                .code("DEP-1");
    }

    public static DepartmentEntity.DepartmentEntityBuilder departmentWithCode(String code) {
        return DepartmentEntity.builder()
                .name("Department 1")
                .code(code);
    }
}
