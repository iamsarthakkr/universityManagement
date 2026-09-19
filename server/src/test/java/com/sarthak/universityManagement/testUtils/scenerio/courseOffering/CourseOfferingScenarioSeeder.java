package com.sarthak.universityManagement.testUtils.scenerio.courseOffering;

import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import com.sarthak.universityManagement.testUtils.scenerio.course.CourseScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.instructor.InstructorScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.semester.SemesterScenarioSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseOfferingSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;

@TestComponent
@Profile("test")
@RequiredArgsConstructor
public class CourseOfferingScenarioSeeder {
    private final CourseOfferingSeeder courseOfferingSeeder;
    private final CourseScenarioSeeder courseScenarioSeeder;
    private final InstructorScenarioSeeder instructorScenarioSeeder;
    private final SemesterScenarioSeeder semesterScenarioSeeder;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private final CourseOfferingEntity.CourseOfferingEntityBuilder courseOfferingBuilder;
        private final CourseScenarioSeeder.Scenario courseScenarioBuilder;
        private final InstructorScenarioSeeder.Scenario instructorScenarioBuilder;
        private final SemesterScenarioSeeder.Scenario semesterScenarioBuilder;

        private Scenario() {
            this.courseOfferingBuilder = CourseOfferingEntity.builder()
                .section("A")
                .capacity(10)
                .enrolled(0);

            this.courseScenarioBuilder = courseScenarioSeeder.builder();
            this.instructorScenarioBuilder = instructorScenarioSeeder.builder();
            this.semesterScenarioBuilder = semesterScenarioSeeder.builder();
        }

        public Scenario section(String section) {
            courseOfferingBuilder.section(section);
            return this;
        }
        public Scenario enrolled(int enrolled) {
            courseOfferingBuilder.enrolled(enrolled);
            return this;
        }
        public Scenario capacity(int capacity) {
            courseOfferingBuilder.capacity(capacity);
            return this;
        }

        public Scenario course(Consumer<CourseScenarioSeeder.Scenario> courseScenarioConsumer) {
            courseScenarioConsumer.accept(courseScenarioBuilder);
            return this;
        }
        public Scenario instructor(Consumer<InstructorScenarioSeeder.Scenario> instructorScenarioConsumer) {
            instructorScenarioConsumer.accept(instructorScenarioBuilder);
            return this;
        }
        public Scenario semester(Consumer<SemesterScenarioSeeder.Scenario> semesterScenarioConsumer) {
            semesterScenarioConsumer.accept(semesterScenarioBuilder);
            return this;
        }

        public CourseOfferingScenario build() {

            var instructorScenario = instructorScenarioBuilder.build();
            var courseScenario = courseScenarioBuilder.build();
            var semesterScenario = semesterScenarioBuilder.build();

            var courseOffering = courseOfferingSeeder.seedOrGet(courseOfferingBuilder);

            return new CourseOfferingScenario(
                courseOffering,
                courseScenario.course(),
                semesterScenario.semester(),
                instructorScenario.instructor(),
                courseScenario.department()
            );

        }
    }
}
