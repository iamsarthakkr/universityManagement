package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.student.StudentEntity;

import java.time.LocalDate;

public final class StudentFixtures {

    public static StudentEntity.StudentEntityBuilder student(DepartmentEntity department) {
        return StudentEntity.builder()
            .firstName("jon")
            .lastName("doe")
            .dateOfBirth(LocalDate.of(1999, 12, 31))
            .department(department);
    }

}
