package com.sarthak.universityManagement.testUtils.scenerio.enrollment;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.testUtils.fixtures.EnrollmentFixtures;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.EnrollmentSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class EnrollmentScenarioSeeder {
    private final EnrollmentSeeder enrollmentSeeder;
    private final StudentScenarioSeeder studentScenarioSeeder;
    private final CourseOfferingScenarioSeeder courseOfferingScenarioSeeder;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private final StudentScenarioSeeder.Scenario studentScenarioBuilder = studentScenarioSeeder.builder();
        private final CourseOfferingScenarioSeeder.Scenario courseOfferingScenarioBuilder = courseOfferingScenarioSeeder.builder();

        private int departmentNumber = 1;
        private EnrollmentStatus enrollmentStatus = EnrollmentStatus.PENDING;

        public Scenario enrollmentStatus(EnrollmentStatus enrollmentStatus) {
            this.enrollmentStatus = enrollmentStatus;
            return this;
        }

        public Scenario student(Consumer<StudentScenarioSeeder.Scenario> studentScenarioConsumer) {
            studentScenarioConsumer.accept(studentScenarioBuilder);
            return this;
        }

        public Scenario offering(Consumer<CourseOfferingScenarioSeeder.Scenario> courseOfferingScenarioConsumer) {
            courseOfferingScenarioConsumer.accept(courseOfferingScenarioBuilder);
            return this;
        }

        public Scenario departmentNumber(int departmentNumber) {
            this.departmentNumber = departmentNumber;
            return this;
        }

        public EnrollmentScenario build() {
            var studentScenario = studentScenarioBuilder
                .department(department -> department.departmentNumber(departmentNumber))
                .build();
            var courseOfferingScenario = courseOfferingScenarioBuilder
                .departmentNumber(departmentNumber)
                .build();

            var enrollmentBuilder = EnrollmentFixtures
                .enrollment(studentScenario.student(), courseOfferingScenario.courseOffering())
                .status(enrollmentStatus);

            var enrollment = enrollmentSeeder.seedOrGet(enrollmentBuilder);

            return new EnrollmentScenario(
                enrollment,
                studentScenario.student(),
                courseOfferingScenario.courseOffering(),
                courseOfferingScenario.course(),
                courseOfferingScenario.instructor(),
                courseOfferingScenario.department(),
                courseOfferingScenario.semester()
            );
        }

    }
}
