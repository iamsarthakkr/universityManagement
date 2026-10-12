package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.security.AuthOutcome;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.InstructorRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class InstructorRegistrationServiceAuthorizationTests extends IntegrationTests {
    private static final RegistrationFilter NO_FILTER = new RegistrationFilter(null, null);

    @Autowired
    private InstructorRegistrationService service;
    @Autowired
    private InstructorRegistrationRepo registrationRepo;
    @Autowired
    private InstructorRegistrationSeeder registrationSeeder;
    @Autowired
    private UserSeeder userSeeder;

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    static Stream<Arguments> adminOnly() {
        return Stream.of(
            Arguments.of(RoleActor.ADMIN, AuthOutcome.ALLOWED),
            Arguments.of(RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
            Arguments.of(RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
            Arguments.of(RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED)
        );
    }

    private void authenticate(RoleActor actor) {
        if(actor == RoleActor.ADMIN) {
            TestAuthentication.asUser(userSeeder.saveUser(UserFixtures.user().username("admin").email("admin@test.com").role(Role.ADMIN).build()));
        } else {
            actor.authenticate();
        }
    }

    @Nested
    class ListAuthorization {

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("com.sarthak.universityManagement.registration.instructor.InstructorRegistrationServiceAuthorizationTests#adminOnly")
        void shouldOnlyLetAdminListRegistrations(RoleActor actor, AuthOutcome outcome) {
            registrationSeeder.saveDefaultInstructorRegistration("dep-test");
            authenticate(actor);

            if(outcome == AuthOutcome.ALLOWED) {
                assertEquals(1, service.getRequests(NO_FILTER).size());
            } else {
                assertThrows(outcome.expectedException(), () -> service.getRequests(NO_FILTER));
            }
        }
    }

    @Nested
    class ActionAuthorization {

        static Stream<Arguments> cases() {
            return adminOnly().flatMap(row -> Stream.of(RegistrationAction.values())
                .map(action -> Arguments.of(row.get()[0], action, row.get()[1])));
        }

        @ParameterizedTest(name = "{0} {1} -> {2}")
        @MethodSource("cases")
        void shouldOnlyLetAdminReviewRegistrations(RoleActor actor, RegistrationAction action, AuthOutcome outcome) {
            var saved = registrationSeeder.saveDefaultInstructorRegistration("dep-test");
            authenticate(actor);

            if(outcome == AuthOutcome.ALLOWED) {
                assertEquals(action.getTargetStatus(), service.performAction(saved.getId(), action).status());
            } else {
                assertThrows(outcome.expectedException(), () -> service.performAction(saved.getId(), action));
                assertEquals(RegistrationStatus.PENDING, registrationRepo.findById(saved.getId()).orElseThrow().getRegistrationStatus());
            }
        }
    }
}
