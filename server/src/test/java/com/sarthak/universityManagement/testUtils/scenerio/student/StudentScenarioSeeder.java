package com.sarthak.universityManagement.testUtils.scenerio.student;

import com.sarthak.universityManagement.testUtils.scenerio.department.DepartmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class StudentScenarioSeeder {
    private static final String STUDENT_PREFIX = "test-student-";
    private final DepartmentScenarioSeeder departmentScenarioSeeder;
    private final StudentSeeder studentSeeder;

    public class Scenario {
        private int studentNumber = 1;
        private int departmentNumber = 1;

        public Scenario studentNumber(int studentNumber) {
            this.studentNumber = studentNumber;
            return this;
        }

        public Scenario departmentNumber(int departmentNumber) {
            this.departmentNumber = departmentNumber;
            return this;
        }

        public StudentScenario build() {
            var departmentScenario = departmentScenarioSeeder.builder()
                .departmentNumber(departmentNumber)
                .build();

            var username = STUDENT_PREFIX + studentNumber;
            var student = studentSeeder.seedOrGet(departmentScenario.department(), username);

            return new StudentScenario(
                student,
                departmentScenario.department()
            );
        }

    }
}
