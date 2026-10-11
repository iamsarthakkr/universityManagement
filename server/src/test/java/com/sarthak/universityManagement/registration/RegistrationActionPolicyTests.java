package com.sarthak.universityManagement.registration;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationActionPolicyTests {

    static Stream<Arguments> allowedActionsCases() {
        List<Arguments> cases = new ArrayList<>(List.of(
            Arguments.of(RegistrationStatus.PENDING, Role.ADMIN, List.of(RegistrationAction.APPROVE, RegistrationAction.REJECT)),
            Arguments.of(RegistrationStatus.APPROVED, Role.ADMIN, List.of()),
            Arguments.of(RegistrationStatus.REJECTED, Role.ADMIN, List.of())
        ));

        for(var role: List.of(Role.INSTRUCTOR, Role.STUDENT)) {
            for(var status: RegistrationStatus.values()) {
                cases.add(Arguments.of(status, role, List.of()));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{1} on {0} -> {2}")
    @MethodSource("allowedActionsCases")
    void shouldListAllowedActions(RegistrationStatus status, Role role, List<RegistrationAction> expected) {
        assertEquals(expected, RegistrationActionPolicy.allowedActions(status, role));
    }
}
