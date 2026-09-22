package com.sarthak.universityManagement.testUtils.scenerio.course;

import com.sarthak.universityManagement.testUtils.scenerio.department.DepartmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class CourseScenarioSeeder {
    private static final String COURSE_CODE_PREFIX = "TEST";

    private final DepartmentScenarioSeeder departmentScenarioSeeder;
    private final CourseSeeder courseSeeder;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private int courseNumber = 1;
        private final DepartmentScenarioSeeder.Scenario departmentScenarioBuilder = departmentScenarioSeeder.builder();

        public Scenario courseNumber(int courseNumber) {
            this.courseNumber = courseNumber;
            return this;
        }

        public Scenario department(Consumer<DepartmentScenarioSeeder.Scenario> departmentScenarioConsumer) {
            departmentScenarioConsumer.accept(departmentScenarioBuilder);
            return this;
        }

        public CourseScenario build() {
            var departmentScenario = departmentScenarioBuilder.build();

            var courseCode = COURSE_CODE_PREFIX + courseNumber;
            var course = courseSeeder.seedOrGet(departmentScenario.department(), courseCode);

            return new CourseScenario(
                departmentScenario.department(),
                course
            );
        }

    }
}
