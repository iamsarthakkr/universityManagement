package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.validators.SemesterValidator;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterValidatorTests {

    @Nested
    class Dates {
        private static final LocalDate DAY = LocalDate.of(2026, 1, 15);
        private static final String REGISTRATION_MESSAGE = "Registration start date must be before registration end date";
        private static final String SEMESTER_MESSAGE = "Start date must be before end date";

        static Stream<Arguments> invalidDateCases() {
            return Stream.of(
                Arguments.of("registration starts after it ends",
                    SemesterFixtures.semesterRequest().registrationStartDate(DAY.plusDays(1)).registrationEndDate(DAY).build(),
                    REGISTRATION_MESSAGE),
                Arguments.of("registration starts and ends on the same day",
                    SemesterFixtures.semesterRequest().registrationStartDate(DAY).registrationEndDate(DAY).build(),
                    REGISTRATION_MESSAGE),
                Arguments.of("semester starts after it ends",
                    SemesterFixtures.semesterRequest().startDate(DAY.plusDays(1)).endDate(DAY).build(),
                    SEMESTER_MESSAGE),
                Arguments.of("semester starts and ends on the same day",
                    SemesterFixtures.semesterRequest().startDate(DAY).endDate(DAY).build(),
                    SEMESTER_MESSAGE)
            );
        }

        @Test
        void shouldAcceptValidDates() {
            assertDoesNotThrow(() -> SemesterValidator.validateSemesterDates(SemesterFixtures.semesterRequest().build()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidDateCases")
        void shouldRejectInvalidDates(String label, CreateSemesterRequest request, String expectedMessage) {
            var ex = assertThrows(BadRequestException.class, () -> SemesterValidator.validateSemesterDates(request));

            assertEquals(expectedMessage, ex.getMessage());
        }
    }

    @Nested
    class Offerings {

        @ParameterizedTest
        @EnumSource(value = SemesterStatus.class, names = {"PLANNED"})
        void shouldAllowOffering(SemesterStatus status) {
            var entity = SemesterFixtures.semester().status(status).build();

            assertDoesNotThrow(() -> SemesterValidator.validateSemesterAllowsOfferings(entity));
        }

        @ParameterizedTest
        @EnumSource(value = SemesterStatus.class, names = {"PLANNED"}, mode = EnumSource.Mode.EXCLUDE)
        void shouldNotAllowOffering(SemesterStatus status) {
            var entity = SemesterFixtures.semester().status(status).build();

            assertThrows(BadRequestException.class, () -> SemesterValidator.validateSemesterAllowsOfferings(entity));
        }
    }
}
