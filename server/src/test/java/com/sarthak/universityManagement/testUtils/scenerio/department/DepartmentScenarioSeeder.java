package com.sarthak.universityManagement.testUtils.scenerio.department;

import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class DepartmentScenarioSeeder {
    private static final String DEPARTMENT_CODE_PREFIX = "DEP";
    private final DepartmentSeeder departmentSeeder;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private int departmentNumber = 1;

        public Scenario departmentNumber(int departmentNumber) {
            this.departmentNumber = departmentNumber;
            return this;
        }

        public DepartmentScenario build() {
            var departmentCode = DEPARTMENT_CODE_PREFIX + departmentNumber;
            var department = departmentSeeder.seedOrGet(departmentCode);

            return new DepartmentScenario(
                department
            );
        }

    }
}
