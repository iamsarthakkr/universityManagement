package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;

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

    enum TestActor { ADMIN, INSTRUCTOR, STUDENT, ANONYMOUS }
    enum Outcome { ALLOWED, ACCESS_DENIED, UNAUTHENTICATED }

    private void authenticateAs(TestActor actor) {
        switch (actor) {
            case ADMIN -> TestAuthentication.asRole(Role.ADMIN);
            case INSTRUCTOR -> TestAuthentication.asRole(Role.INSTRUCTOR);
            case STUDENT -> TestAuthentication.asRole(Role.STUDENT);
            case ANONYMOUS -> TestAuthentication.clear();
        }
    }

    private static Class<? extends Exception> expectedException(Outcome outcome) {
        return switch (outcome) {
            case ACCESS_DENIED -> AuthorizationDeniedException.class;
            case UNAUTHENTICATED -> AuthenticationCredentialsNotFoundException.class;
            case ALLOWED -> throw new IllegalArgumentException("ALLOWED has no exception");
        };
    }

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    @Nested
    class CreationAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(TestActor.ADMIN, Outcome.ALLOWED),
                Arguments.of(TestActor.INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.of(TestActor.STUDENT, Outcome.ACCESS_DENIED),
                Arguments.of(TestActor.ANONYMOUS, Outcome.UNAUTHENTICATED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldAuthorizeCreation(TestActor actor, Outcome outcome) {
            var request = SemesterFixtures.semesterRequest().build();
            authenticateAs(actor);

            if(outcome == Outcome.ALLOWED) {
                assertNotNull(semesterService.createSemester(request).id());
            } else {
                assertThrows(expectedException(outcome), () -> semesterService.createSemester(request));
                assertEquals(0, semesterRepo.count());
            }
        }
    }

    @Nested
    class ActionAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(SemesterAction.ACTIVATE, TestActor.ADMIN, Outcome.ALLOWED),
                Arguments.of(SemesterAction.ACTIVATE, TestActor.INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.ACTIVATE, TestActor.STUDENT, Outcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.ACTIVATE, TestActor.ANONYMOUS, Outcome.UNAUTHENTICATED),

                Arguments.of(SemesterAction.COMPLETE, TestActor.ADMIN, Outcome.ALLOWED),
                Arguments.of(SemesterAction.COMPLETE, TestActor.INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.COMPLETE, TestActor.STUDENT, Outcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.COMPLETE, TestActor.ANONYMOUS, Outcome.UNAUTHENTICATED),

                Arguments.of(SemesterAction.CANCEL, TestActor.ADMIN, Outcome.ALLOWED),
                Arguments.of(SemesterAction.CANCEL, TestActor.INSTRUCTOR, Outcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.CANCEL, TestActor.STUDENT, Outcome.ACCESS_DENIED),
                Arguments.of(SemesterAction.CANCEL, TestActor.ANONYMOUS, Outcome.UNAUTHENTICATED)
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
        void shouldAuthorizeAction(SemesterAction action, TestActor actor, Outcome outcome) {
            var from = getValidStartingStatus(action);
            var semester = semesterSeeder.saveSemester(SemesterFixtures.semester().status(from).build());
            authenticateAs(actor);

            if(outcome == Outcome.ALLOWED) {
                assertEquals(action.getTargetStatus(), semesterService.transition(semester.getId(), action).status());
            } else {
                assertThrows(expectedException(outcome), () -> semesterService.transition(semester.getId(), action));
                assertEquals(from, semesterRepo.findById(semester.getId()).orElseThrow().getStatus());
            }
        }
    }

    @Nested
    class ReadVisibility {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(TestActor.ADMIN, List.of(SemesterAction.ACTIVATE, SemesterAction.CANCEL)),
                Arguments.of(TestActor.INSTRUCTOR, List.of()),
                Arguments.of(TestActor.STUDENT, List.of())
            );
        }

        @ParameterizedTest(name = "{0} sees {1}")
        @MethodSource("cases")
        void shouldReadSemesterWithRoleSpecificActions(TestActor actor, List<SemesterAction> expectedActions) {
            var semester = semesterSeeder.saveSemester(SemesterFixtures.semester().status(SemesterStatus.PLANNED).build());
            authenticateAs(actor);

            assertEquals(expectedActions, semesterService.getSemester(semester.getId()).allowedActions());

            var listed = semesterService.getSemesters();
            assertEquals(1, listed.size());
            assertEquals(expectedActions, listed.getFirst().allowedActions());
        }
    }
}
