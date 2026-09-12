package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Transactional
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
        @EnumSource(
            value = SemesterStatus.class,
            names = {"ACTIVE", "CANCELLED"}
        )
        void shouldTransitionFromPlanned(SemesterStatus newStatus) {
            var semesterEntity = SemesterFixtures.semester().status(SemesterStatus.PLANNED).build();

            assertTrue(semesterEntity.canTransitionTo(newStatus));
        }

        @ParameterizedTest
        @EnumSource(
            value = SemesterStatus.class,
            names = {"COMPLETED", "CANCELLED"}
        )
        void shouldTransitionFromActive(SemesterStatus newStatus) {
            var semesterEntity = SemesterFixtures.semester().status(SemesterStatus.ACTIVE).build();

            assertTrue(semesterEntity.canTransitionTo(newStatus));
        }

        @Test
        void shouldNotTransitionFromActive() {
            var semesterEntity = SemesterFixtures.semester().status(SemesterStatus.ACTIVE).build();

            assertFalse(semesterEntity.canTransitionTo(SemesterStatus.PLANNED));
        }


        @ParameterizedTest
        @EnumSource(
            value = SemesterStatus.class,
            names = {"PLANNED", "ACTIVE", "CANCELLED"}
        )
        void shouldNotTransitionFromCompleted(SemesterStatus newStatus) {
            var semesterEntity = SemesterFixtures.semester().status(SemesterStatus.COMPLETED).build();

            assertFalse(semesterEntity.canTransitionTo(newStatus));
        }

        @ParameterizedTest
        @EnumSource(
            value = SemesterStatus.class,
            names = {"PLANNED", "ACTIVE", "COMPLETED"}
        )
        void shouldNotTransitionFromCancelled(SemesterStatus newStatus) {
            var semesterEntity = SemesterFixtures.semester().status(SemesterStatus.CANCELLED).build();

            assertFalse(semesterEntity.canTransitionTo(newStatus));
        }
    }

}
