package com.sarthak.universityManagement.testUtils.scenerio.course;

import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class CourseScenarioSeeder {
    private static final String COURSE_CODE_PREFIX = "TEST";
    private static final String DEPARTMENT_CODE_PREFIX = "DEP";

    private final DepartmentSeeder departmentSeeder;
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
            var departmentCode = DEPARTMENT_CODE_PREFIX + departmentNumber;
            var department = departmentSeeder.seedOrGet(departmentCode);

            var courseCode = COURSE_CODE_PREFIX + courseNumber;
            var course = courseSeeder.seedOrGet(department, courseCode);

            return new CourseScenario(
                department,
                course
            );
        }

    }
}
