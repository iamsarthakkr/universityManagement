package com.sarthak.universityManagement.testUtils.scenerio.course;

import com.sarthak.universityManagement.testUtils.scenerio.department.DepartmentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class CourseScenarioSeeder {
    private static final String COURSE_CODE_PREFIX = "TEST";

    private final DepartmentScenarioSeeder departmentScenarioSeeder;
    private final CourseSeeder courseSeeder;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private int departmentNumber = 1;
        private int courseNumber = 1;

        public Scenario departmentNumber(int departmentNumber) {
            this.departmentNumber = departmentNumber;
            return this;
        }

        public Scenario courseNumber(int courseNumber) {
            this.courseNumber = courseNumber;
            return this;
        }

        public CourseScenario build() {
            var departmentScenario = departmentScenarioSeeder.builder()
                .departmentNumber(departmentNumber)
                .build();

            var courseCode = COURSE_CODE_PREFIX + courseNumber;
            var course = courseSeeder.seedOrGet(departmentScenario.department(), courseCode);

            return new CourseScenario(
                departmentScenario.department(),
                course
            );
        }

    }
}
