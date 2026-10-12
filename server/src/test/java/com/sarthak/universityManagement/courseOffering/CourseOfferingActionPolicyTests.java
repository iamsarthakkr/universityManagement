package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.courseOffering.types.CourseOfferingAction;
import com.sarthak.universityManagement.courseOffering.types.EnrollDenial;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CourseOfferingActionPolicyTests {

    // The fixture semester's registration window is 2026-01-01 .. 2026-01-30
    private static final LocalDate WINDOW_OPEN = LocalDate.of(2026, 1, 15);
    private static final LocalDate WINDOW_CLOSED = LocalDate.of(2026, 2, 10);

    private static CourseOfferingEntity offering(boolean full) {
        return CourseOfferingFixtures.courseOffering(null, null, SemesterFixtures.semester().build())
            .capacity(10)
            .enrolled(full ? 10 : 9)
            .build();
    }

    // role × registration open × full; the first failing check decides the denial.
    // A student's existing request is deliberately not part of the policy: ENROLL reflects the offering only,
    // and a duplicate is rejected when the enrollment is created
    static Stream<Arguments> enrollCases() {
        List<Arguments> cases = new ArrayList<>(List.of(
            Arguments.of(Role.STUDENT, true, false, null),
            Arguments.of(Role.STUDENT, true, true, EnrollDenial.OFFERING_FULL),
            Arguments.of(Role.STUDENT, false, false, EnrollDenial.REGISTRATION_CLOSED),
            Arguments.of(Role.STUDENT, false, true, EnrollDenial.REGISTRATION_CLOSED)
        ));

        for(var role: List.of(Role.ADMIN, Role.INSTRUCTOR)) {
            for(var open: List.of(true, false)) {
                for(var full: List.of(true, false)) {
                    cases.add(Arguments.of(role, open, full, EnrollDenial.NOT_A_STUDENT));
                }
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}, open={1}, full={2} -> {3}")
    @MethodSource("enrollCases")
    void shouldValidateEnroll(Role role, boolean open, boolean full, EnrollDenial expected) {
        var today = open ? WINDOW_OPEN : WINDOW_CLOSED;

        assertEquals(Optional.ofNullable(expected), CourseOfferingActionPolicy.validateEnroll(offering(full), role, today));
    }

    @ParameterizedTest(name = "{0}, open={1}, full={2}")
    @MethodSource("enrollCases")
    void allowedActionsShouldAgreeWithValidateEnroll(Role role, boolean open, boolean full, EnrollDenial expected) {
        var today = open ? WINDOW_OPEN : WINDOW_CLOSED;
        var expectedActions = expected == null ? List.of(CourseOfferingAction.ENROLL) : List.of();

        assertEquals(expectedActions, CourseOfferingActionPolicy.allowedActions(offering(full), role, today));
    }
}
