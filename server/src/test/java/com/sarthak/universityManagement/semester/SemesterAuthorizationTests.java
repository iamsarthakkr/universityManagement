package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.AuthOutcome;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterAuthorizationTests extends IntegrationTests {

    @Autowired
    private SemesterService semesterService;
    @Autowired
    private SemesterRepo semesterRepo;
    @Autowired
    private SemesterSeeder semesterSeeder;

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    @Nested
    class CreationAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(RoleActor.ADMIN, AuthOutcome.ALLOWED),
                Arguments.of(RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldAuthorizeCreation(RoleActor actor, AuthOutcome outcome) {
            var request = SemesterFixtures.semesterRequest().build();
            actor.authenticate();

            if(outcome == AuthOutcome.ALLOWED) {
                assertNotNull(semesterService.createSemester(request).id());
            } else {
                assertThrows(outcome.expectedException(), () -> semesterService.createSemester(request));
                assertEquals(0, semesterRepo.count());
            }
        }
    }

    @Nested
    class ActionAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(SemesterAction.ACTIVATE, RoleActor.ADMIN, AuthOutcome.ALLOWED),
                Arguments.of(SemesterAction.ACTIVATE, RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.ACTIVATE, RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.ACTIVATE, RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED),

                Arguments.of(SemesterAction.COMPLETE, RoleActor.ADMIN, AuthOutcome.ALLOWED),
                Arguments.of(SemesterAction.COMPLETE, RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.COMPLETE, RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.COMPLETE, RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED),

                Arguments.of(SemesterAction.CANCEL, RoleActor.ADMIN, AuthOutcome.ALLOWED),
                Arguments.of(SemesterAction.CANCEL, RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.CANCEL, RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.CANCEL, RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED)
            );
        }

        private static SemesterStatus getValidStartingStatus(SemesterAction action) {
            return switch (action) {
                case ACTIVATE, CANCEL -> SemesterStatus.PLANNED;
                case COMPLETE -> SemesterStatus.ACTIVE;
            };
        }

        @ParameterizedTest(name = "{1} {0} -> {2}")
        @MethodSource("cases")
        void shouldAuthorizeAction(SemesterAction action, RoleActor actor, AuthOutcome outcome) {
            var from = getValidStartingStatus(action);
            var semester = semesterSeeder.saveSemester(SemesterFixtures.semester().status(from).build());
            actor.authenticate();

            if(outcome == AuthOutcome.ALLOWED) {
                assertEquals(action.getTargetStatus(), semesterService.transition(semester.getId(), action).status());
            } else {
                assertThrows(outcome.expectedException(), () -> semesterService.transition(semester.getId(), action));
                assertEquals(from, semesterRepo.findById(semester.getId()).orElseThrow().getStatus());
            }
        }
    }

    @Nested
    class ReadVisibility {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(RoleActor.ADMIN, List.of(SemesterAction.ACTIVATE, SemesterAction.CANCEL)),
                Arguments.of(RoleActor.INSTRUCTOR, List.of()),
                Arguments.of(RoleActor.STUDENT, List.of())
            );
        }

        @ParameterizedTest(name = "{0} sees {1}")
        @MethodSource("cases")
        void shouldReadSemesterWithRoleSpecificActions(RoleActor actor, List<SemesterAction> expectedActions) {
            var semester = semesterSeeder.saveSemester(SemesterFixtures.semester().status(SemesterStatus.PLANNED).build());
            actor.authenticate();

            assertEquals(expectedActions, semesterService.getSemester(semester.getId()).allowedActions());

            var listed = semesterService.getSemesters();
            assertEquals(1, listed.size());
            assertEquals(expectedActions, listed.getFirst().allowedActions());
        }
    }
}
