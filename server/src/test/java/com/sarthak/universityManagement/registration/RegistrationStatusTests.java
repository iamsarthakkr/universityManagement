package com.sarthak.universityManagement.registration;

import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RegistrationStatusTests {

    static final List<List<RegistrationStatus>> VALID = List.of(
        List.of(RegistrationStatus.PENDING, RegistrationStatus.APPROVED),
        List.of(RegistrationStatus.PENDING, RegistrationStatus.REJECTED)
    );

    static Stream<Arguments> validTransitions() {
        return VALID.stream().map(pair -> Arguments.of(pair.get(0), pair.get(1)));
    }

    static Stream<Arguments> invalidTransitions() {
        List<Arguments> cases = new ArrayList<>();
        for(var from: RegistrationStatus.values()) {
            for(var to: RegistrationStatus.values()) {
                if(!VALID.contains(List.of(from, to))) cases.add(Arguments.of(from, to));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("validTransitions")
    void shouldAllowValidTransitions(RegistrationStatus from, RegistrationStatus to) {
        assertTrue(from.canTransitionTo(to));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransitions(RegistrationStatus from, RegistrationStatus to) {
        assertFalse(from.canTransitionTo(to));
    }
}
