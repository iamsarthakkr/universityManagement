package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterIntegrationTests extends IntegrationTests {
    private static final int MISSING_SEMESTER_ID = Integer.MAX_VALUE;

    @Autowired
    private SemesterService semesterService;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private Clock clock;

    @Nested
    @WithAdmin
    class Creation {

        @Test
        void shouldCreatePlannedSemesterWithCorrectFields() {
            var semesterRequest = SemesterFixtures.semesterRequest().build();

            var resp = semesterService.createSemester(semesterRequest);

            assertNotNull(resp.id());
            assertEquals(semesterRequest.term(), resp.term());
            assertEquals(semesterRequest.year(), resp.year());
            assertEquals(semesterRequest.registrationStartDate(), resp.registrationStartDate());
            assertEquals(semesterRequest.registrationEndDate(), resp.registrationEndDate());
            assertEquals(semesterRequest.startDate(), resp.startDate());
            assertEquals(semesterRequest.endDate(), resp.endDate());

            assertEquals(SemesterStatus.PLANNED, resp.status());
            assertEquals(List.of(SemesterAction.ACTIVATE, SemesterAction.CANCEL), resp.allowedActions());
        }

        @Test
        void shouldRejectDuplicateTermAndYear() {
            semesterService.createSemester(SemesterFixtures.semesterRequest().term(SemesterTerm.WINTER).year(2027).build());

            var duplicate = SemesterFixtures.semesterRequest().term(SemesterTerm.WINTER).year(2027).build();
            assertThrows(ConflictException.class, () -> semesterService.createSemester(duplicate));
        }

        @Test
        void shouldRejectInvalidDatesBeforeSaving() {
            var request = SemesterFixtures.semesterRequest()
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 2, 1))
                .build();

            assertThrows(BadRequestException.class, () -> semesterService.createSemester(request));
            assertTrue(semesterService.getSemesters().isEmpty());
        }
    }

    @Nested
    @WithAdmin
    class Fetch {

        @Test
        void shouldFetchSemester() {
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);

            var got = semesterService.getSemester(sem.getId());

            assertEquals(sem.getId(), got.id());
            assertEquals(sem.getTerm(), got.term());
            assertEquals(sem.getYear(), got.year());
        }

        @Test
        void shouldFetchAllSemesters() {
            semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2026);
            semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2027);
            semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2027);

            assertEquals(4, semesterService.getSemesters().size());
        }

        @Test
        void shouldReportRegistrationOpenUsingTheClock() {
            var today = LocalDate.now(clock);
            var open = semesterSeeder.saveSemester(SemesterFixtures.semester()
                .term(SemesterTerm.SUMMER)
                .registrationStartDate(today.minusDays(1))
                .registrationEndDate(today.plusDays(1))
                .startDate(today.plusDays(2))
                .endDate(today.plusDays(30))
                .build());
            var closed = semesterSeeder.saveSemester(SemesterFixtures.semester()
                .term(SemesterTerm.WINTER)
                .registrationStartDate(today.plusDays(1))
                .registrationEndDate(today.plusDays(2))
                .startDate(today.plusDays(3))
                .endDate(today.plusDays(30))
                .build());

            assertTrue(semesterService.getSemester(open.getId()).isRegistrationOpen());
            assertFalse(semesterService.getSemester(closed.getId()).isRegistrationOpen());
        }

        @Test
        void shouldThrowWhenSemesterNotFound() {
            assertThrows(ResourceNotFoundException.class, () -> semesterService.getSemester(MISSING_SEMESTER_ID));
        }
    }

    @Nested
    @WithAdmin
    class Transition {

        record TransitionCase(
            SemesterStatus from,
            SemesterAction action
        ) {}

        static Stream<TransitionCase> validTransitionCases() {
            return Stream.of(
                new TransitionCase(SemesterStatus.PLANNED, SemesterAction.ACTIVATE),
                new TransitionCase(SemesterStatus.PLANNED, SemesterAction.CANCEL),
                new TransitionCase(SemesterStatus.ACTIVE, SemesterAction.COMPLETE),
                new TransitionCase(SemesterStatus.ACTIVE, SemesterAction.CANCEL)
            );
        }

        static Stream<TransitionCase> invalidTransitionCases() {
            var valid = validTransitionCases().collect(Collectors.toSet());

            List<TransitionCase> invalidTransitions = new ArrayList<>();
            for(var from: SemesterStatus.values()) {
                for(var action: SemesterAction.values()) {
                    if(!valid.contains(new TransitionCase(from, action))) {
                        invalidTransitions.add(new TransitionCase(from, action));
                    }
                }
            }

            return invalidTransitions.stream();
        }

        private static List<SemesterAction> adminActionsFor(SemesterStatus status) {
            return switch (status) {
                case PLANNED -> List.of(SemesterAction.ACTIVATE, SemesterAction.CANCEL);
                case ACTIVE -> List.of(SemesterAction.COMPLETE, SemesterAction.CANCEL);
                case COMPLETED, CANCELLED -> List.of();
            };
        }

        private SemesterEntity seed(SemesterStatus status) {
            return semesterSeeder.saveSemester(SemesterFixtures.semester().status(status).build());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("validTransitionCases")
        void shouldAllowSemesterTransition(TransitionCase transitionCase) {
            var sem = seed(transitionCase.from());
            assertTrue(semesterService.getSemester(sem.getId()).allowedActions().contains(transitionCase.action()));

            var response = semesterService.transition(sem.getId(), transitionCase.action());

            var target = transitionCase.action().getTargetStatus();
            assertEquals(target, response.status());
            assertEquals(target, semesterService.getSemester(sem.getId()).status());
            assertEquals(adminActionsFor(target), response.allowedActions());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTransitionCases")
        void shouldDenySemesterTransition(TransitionCase transitionCase) {
            var sem = seed(transitionCase.from());
            assertFalse(semesterService.getSemester(sem.getId()).allowedActions().contains(transitionCase.action()));

            assertThrows(BadRequestException.class, () -> semesterService.transition(sem.getId(), transitionCase.action()));

            assertEquals(transitionCase.from(), semesterService.getSemester(sem.getId()).status());
        }

        @Test
        void shouldWalkThroughTheFullLifecycle() {
            var sem = seed(SemesterStatus.PLANNED);

            semesterService.transition(sem.getId(), SemesterAction.ACTIVATE);
            var completed = semesterService.transition(sem.getId(), SemesterAction.COMPLETE);

            assertEquals(SemesterStatus.COMPLETED, completed.status());
            assertTrue(completed.allowedActions().isEmpty());
        }

        @Test
        void shouldThrowWhenSemesterNotFound() {
            assertThrows(ResourceNotFoundException.class, () -> semesterService.transition(MISSING_SEMESTER_ID, SemesterAction.ACTIVATE));
        }
    }
}
