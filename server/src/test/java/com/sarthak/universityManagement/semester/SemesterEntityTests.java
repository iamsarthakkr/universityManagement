package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterEntityTests {

    @Nested
    class Transition {

        record TransitionCase(
            SemesterStatus from,
            SemesterStatus to
        ) {}

        static Stream<TransitionCase> validTransitionCases() {
            return Stream.of(
                new TransitionCase(SemesterStatus.PLANNED, SemesterStatus.ACTIVE),
                new TransitionCase(SemesterStatus.PLANNED, SemesterStatus.CANCELLED),
                new TransitionCase(SemesterStatus.ACTIVE, SemesterStatus.COMPLETED),
                new TransitionCase(SemesterStatus.ACTIVE, SemesterStatus.CANCELLED)
            );
        }

        static Stream<TransitionCase> invalidTransitionCases() {
            var valid = validTransitionCases().collect(Collectors.toSet());

            List<TransitionCase> invalidTransitions = new ArrayList<>();
            for(var from: SemesterStatus.values()) {
                for(var to: SemesterStatus.values()) {
                    if(!valid.contains(new TransitionCase(from, to))) {
                        invalidTransitions.add(new TransitionCase(from, to));
                    }
                }
            }

            return invalidTransitions.stream();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("validTransitionCases")
        void shouldAllowSemesterTransition(TransitionCase transitionCase) {
            var semester = SemesterFixtures.semester().status(transitionCase.from()).build();

            assertTrue(semester.canTransitionTo(transitionCase.to()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTransitionCases")
        void shouldDenySemesterTransition(TransitionCase transitionCase) {
            var semester = SemesterFixtures.semester().status(transitionCase.from()).build();

            assertFalse(semester.canTransitionTo(transitionCase.to()));
        }
    }

    @Nested
    class AllowsEnrollment {

        @ParameterizedTest
        @CsvSource({
            "PLANNED, true",
            "ACTIVE, true",
            "COMPLETED, false",
            "CANCELLED, false",
        })
        void shouldAllowEnrollmentOnlyForOpenStatuses(SemesterStatus status, boolean expected) {
            var semester = SemesterFixtures.semester().status(status).build();

            assertEquals(expected, semester.allowsEnrollment());
        }
    }

    @Nested
    class RegistrationWindow {
        private static final LocalDate REGISTRATION_START = LocalDate.of(2026, 1, 1);
        private static final LocalDate REGISTRATION_END = LocalDate.of(2026, 2, 1);

        static Stream<Arguments> windowCases() {
            return Stream.of(
                Arguments.of("day before start", REGISTRATION_START.minusDays(1), false),
                Arguments.of("start day", REGISTRATION_START, true),
                Arguments.of("inside window", REGISTRATION_START.plusDays(9), true),
                Arguments.of("end day", REGISTRATION_END, true),
                Arguments.of("day after end", REGISTRATION_END.plusDays(1), false)
            );
        }

        private SemesterEntity semester(SemesterStatus status) {
            return SemesterFixtures.semester()
                .status(status)
                .registrationStartDate(REGISTRATION_START)
                .registrationEndDate(REGISTRATION_END)
                .build();
        }

        @ParameterizedTest(name = "{0}: {1} -> {2}")
        @MethodSource("windowCases")
        void shouldFollowRegistrationWindowWhenSemesterAllowsEnrollment(String label, LocalDate today, boolean expected) {
            for(var status: List.of(SemesterStatus.PLANNED, SemesterStatus.ACTIVE)) {
                assertEquals(expected, semester(status).isRegistrationOpen(today), status.name());
            }
        }

        @ParameterizedTest(name = "{0}: {1}")
        @MethodSource("windowCases")
        void shouldBeClosedWhenSemesterIsFinished(String label, LocalDate today, boolean _insideWindow) {
            for(var status: List.of(SemesterStatus.COMPLETED, SemesterStatus.CANCELLED)) {
                assertFalse(semester(status).isRegistrationOpen(today), status.name());
            }
        }
    }
}
