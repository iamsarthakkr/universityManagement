package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class SemesterActionPolicyTests {

    static Stream<Arguments> allowedActionsCases() {
        List<Arguments> cases = new ArrayList<>(List.of(
            Arguments.of(SemesterStatus.PLANNED, Role.ADMIN, List.of(SemesterAction.ACTIVATE, SemesterAction.CANCEL)),
            Arguments.of(SemesterStatus.ACTIVE, Role.ADMIN, List.of(SemesterAction.COMPLETE, SemesterAction.CANCEL)),
            Arguments.of(SemesterStatus.COMPLETED, Role.ADMIN, List.of()),
            Arguments.of(SemesterStatus.CANCELLED, Role.ADMIN, List.of())
        ));

        for(var role: List.of(Role.INSTRUCTOR, Role.STUDENT)) {
            for(var status: SemesterStatus.values()) {
                cases.add(Arguments.of(status, role, List.of()));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{1} on {0} -> {2}")
    @MethodSource("allowedActionsCases")
    void shouldListAllowedActions(SemesterStatus status, Role role, List<SemesterAction> expected) {
        var semester = SemesterFixtures.semester().status(status).build();

        assertEquals(expected, SemesterActionPolicy.allowedActions(semester, role));
    }

    @ParameterizedTest(name = "{1} on {0}")
    @MethodSource("allowedActionsCases")
    void canPerformShouldAgreeWithAllowedActions(SemesterStatus status, Role role, List<SemesterAction> expected) {
        var semester = SemesterFixtures.semester().status(status).build();

        for(var action: SemesterAction.values()) {
            assertEquals(expected.contains(action), SemesterActionPolicy.canPerform(semester, action, role), action.name());
        }
    }
}
