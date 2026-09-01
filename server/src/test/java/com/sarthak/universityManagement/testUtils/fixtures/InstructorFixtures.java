package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;

public final class InstructorFixtures {

    public static InstructorEntity.InstructorEntityBuilder instructor() {
        return InstructorEntity.builder()
                .firstName("jon")
                .lastName("doe")
                .phoneNumber("11223344");
    }

    public static InstructorEntity.InstructorEntityBuilder instructorWithDepartment(DepartmentEntity department) {
        return instructor().department(department);
    }

}
