package com.sarthak.universityManagement.testUtils.scenerio.instructor;

import com.sarthak.universityManagement.testUtils.scenerio.department.DepartmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class InstructorScenarioSeeder {
    private static final String INSTRUCTOR_PREFIX = "test-instructor--";
    private final DepartmentScenarioSeeder departmentScenarioSeeder;
    private final InstructorSeeder instructorSeeder;

    public class Scenario {
        private int instructorNumber = 1;
        private int departmentNumber = 1;

        public Scenario instructorNumber(int instructorNumber) {
            this.instructorNumber = instructorNumber;
            return this;
        }

        public Scenario departmentNumber(int departmentNumber) {
            this.departmentNumber = departmentNumber;
            return this;
        }

        public InstructorScenario build() {
            var departmentScenario = departmentScenarioSeeder.builder()
                .departmentNumber(departmentNumber)
                .build();

            var username = INSTRUCTOR_PREFIX + instructorNumber;
            var student = instructorSeeder.seedOrGet(departmentScenario.department(), username);

            return new InstructorScenario(
                student,
                departmentScenario.department()
            );
        }

    }
}
