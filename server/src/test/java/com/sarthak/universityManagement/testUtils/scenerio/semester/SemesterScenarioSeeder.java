package com.sarthak.universityManagement.testUtils.scenerio.semester;

import com.sarthak.universityManagement.semester.SemesterRepo;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("test")
@RequiredArgsConstructor
public class SemesterScenarioSeeder {
    private static final LocalDate DEFAULT_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate DEFAULT_END = LocalDate.of(2026, 6, 30);

    private final SemesterRepo semesterRepo;

    public Scenario builder() { return new Scenario(); }

    public class Scenario {
        private SemesterStatus status = SemesterStatus.PLANNED;
        private LocalDate registrationStartDate = DEFAULT_START;
        private LocalDate registrationEndDate = DEFAULT_END;
        private int semesterNumber = 1;

        public Scenario status(SemesterStatus status) {
            this.status = status;
            return this;
        }
        public Scenario semesterNumber(int semesterNumber) {
            this.semesterNumber = semesterNumber;
            return this;
        }

        public Scenario registrationWindow(LocalDate registrationStartDate, LocalDate registrationEndDate) {
            this.registrationStartDate = registrationStartDate;
            this.registrationEndDate = registrationEndDate;
            return this;
        }

        public SemesterScenario build() {
            var semester = semesterRepo
                .findByTermAndYear(SemesterTerm.SUMMER, semesterNumber)
                .orElseGet(() -> semesterRepo.saveAndFlush(
                    SemesterFixtures.semester()
                        .status(status)
                        .year(semesterNumber)
                        .registrationStartDate(registrationStartDate)
                        .registrationEndDate(registrationEndDate)
                        .build()
                ));

            return new SemesterScenario(
                semester
            );
        }

    }
}
