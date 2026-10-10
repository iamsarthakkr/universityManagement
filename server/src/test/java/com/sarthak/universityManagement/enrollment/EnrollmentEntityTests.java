package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentEntityTests {

    @Nested
    class Transition {

        record TransitionCase(
            EnrollmentStatus from,
            EnrollmentStatus to
        ) {}

        static Stream<TransitionCase> validTransitionCases() {
            return Stream.of(
                new TransitionCase(EnrollmentStatus.PENDING, EnrollmentStatus.ENROLLED),
                new TransitionCase(EnrollmentStatus.PENDING, EnrollmentStatus.REJECTED),
                new TransitionCase(EnrollmentStatus.PENDING, EnrollmentStatus.CANCELLED),
                new TransitionCase(EnrollmentStatus.ENROLLED, EnrollmentStatus.DROPPED)
            );
        }

        static Stream<TransitionCase> invalidTransitionCases() {
            var valid = validTransitionCases().collect(Collectors.toSet());

            List<TransitionCase> invalidTransitions = new ArrayList<>();
            for(var from: EnrollmentStatus.values()) {
                for(var to: EnrollmentStatus.values()) {
                    if(!valid.contains(new TransitionCase(from, to))) {
                        invalidTransitions.add(new TransitionCase(from, to));
                    }
                }
            }

            return invalidTransitions.stream();
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("validTransitionCases")
        void shouldAllowEnrollmentTransition(TransitionCase transitionCase) {
            var enrollment = EnrollmentEntity.builder()
                .status(transitionCase.from())
                .build();

            assertTrue(enrollment.canTransitionTo(transitionCase.to()));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidTransitionCases")
        void shouldDenyEnrollmentTransition(TransitionCase invalidTransitionCase) {
            var enrollment = EnrollmentEntity.builder()
                .status(invalidTransitionCase.from)
                .build();

            assertFalse(enrollment.canTransitionTo(invalidTransitionCase.to()));
        }
    }
}
