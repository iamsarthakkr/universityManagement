package com.sarthak.universityManagement.testUtils.scenerio.instructor;

import com.sarthak.universityManagement.testUtils.scenerio.department.DepartmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class InstructorScenarioSeeder {
    private static final String INSTRUCTOR_PREFIX = "test-instructor--";

    private final InstructorSeeder instructorSeeder;
    private final DepartmentScenarioSeeder departmentScenarioSeeder;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private int instructorNumber = 1;
        private final DepartmentScenarioSeeder.Scenario departmentScenarioBuilder = departmentScenarioSeeder.builder();

        public Scenario instructorNumber(int instructorNumber) {
            this.instructorNumber = instructorNumber;
            return this;
        }

        public Scenario department(Consumer<DepartmentScenarioSeeder.Scenario> departmentScenarioConsumer) {
            departmentScenarioConsumer.accept(departmentScenarioBuilder);
            return this;
        }

        public InstructorScenario build() {
            var departmentScenario = departmentScenarioBuilder.build();

            var username = INSTRUCTOR_PREFIX + instructorNumber;
            var instructor = instructorSeeder.seedOrGet(departmentScenario.department(), username);

            return new InstructorScenario(
                instructor,
                departmentScenario.department()
            );
        }

    }
}
