package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterEntityTests {

    @Nested
    class RegistrationWindowTests {

        @ParameterizedTest
        @EnumSource(
            value = SemesterStatus.class,
            names = {"PLANNED", "ACTIVE"}
        )
        void shouldDetermineRegistrationDatesCorrectlyForPlannedOrActiveSemester(SemesterStatus status) {
            var semesterEntity = SemesterFixtures.semester()
                .status(status)
                .registrationStartDate(LocalDate.of(2026, 1, 1))
                .registrationEndDate(LocalDate.of(2026, 2, 1))
                .build();

            assertTrue(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 1, 1)));
            assertTrue(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 2, 1)));
            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2025, 12, 31)));
            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 2, 2)));

            assertTrue(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 1, 10)));
        }

        @ParameterizedTest
        @EnumSource(
            value = SemesterStatus.class,
            names = {"COMPLETED", "CANCELLED"}
        )
        void shouldDetermineRegistrationDatesCorrectlyForCompletedOrCanceledSemester(SemesterStatus status) {
            var semesterEntity = SemesterFixtures.semester()
                .status(status)
                .registrationStartDate(LocalDate.of(2026, 1, 1))
                .registrationEndDate(LocalDate.of(2026, 2, 1))
                .build();

            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 1, 1)));
            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 2, 1)));
            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2025, 12, 31)));
            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 2, 2)));

            assertFalse(semesterEntity.isRegistrationOpen(LocalDate.of(2026, 1, 10)));

        }
    }

    @Nested
    class StatusTransitionTests {

        @ParameterizedTest
        @CsvSource({
            "PLANNED, ACTIVE",
            "PLANNED, CANCELLED",
            "ACTIVE, COMPLETED",
            "ACTIVE, CANCELLED",
        })
        void shouldTransition(SemesterStatus from, SemesterStatus to) {
            var semesterEntity = SemesterFixtures.semester().status(from).build();
            assertTrue(semesterEntity.canTransitionTo(to));
        }

        @ParameterizedTest
        @CsvSource({
            "PLANNED, PLANNED",
            "PLANNED, COMPLETED",
            "ACTIVE, PLANNED",
            "ACTIVE, ACTIVE",
            "COMPLETED, PLANNED",
            "COMPLETED, ACTIVE",
            "COMPLETED, CANCELLED",
            "COMPLETED, COMPLETED",
            "CANCELLED, PLANNED",
            "CANCELLED, ACTIVE",
            "CANCELLED, COMPLETED",
            "CANCELLED, CANCELLED",
        })
        void shouldNotTransition(SemesterStatus from, SemesterStatus to) {
            var semesterEntity = SemesterFixtures.semester().status(from).build();

            assertFalse(semesterEntity.canTransitionTo(to));
        }

    }

}
