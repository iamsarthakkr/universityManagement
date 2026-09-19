package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentEntityTests {

    @Nested
    class Transition {

        @ParameterizedTest
        @CsvSource({
            "PENDING, ENROLLED",
            "PENDING, REJECTED",
            "PENDING, CANCELLED",

            "ENROLLED, DROPPED",
        })
        void shouldAllowEnrollmentTransition(EnrollmentStatus from , EnrollmentStatus to) {
            var enrollment = EnrollmentEntity.builder()
                .status(from)
                .build();

            assertTrue(enrollment.canTransitionTo(to));
        }

        @ParameterizedTest
        @CsvSource({
            "PENDING, PENDING",

            "ENROLLED, PENDING",
            "ENROLLED, ENROLLED",
            "ENROLLED, CANCELLED",
            "ENROLLED, REJECTED",

            "REJECTED, PENDING",
            "REJECTED, ENROLLED",
            "REJECTED, REJECTED",
            "REJECTED, CANCELLED",
            "REJECTED, DROPPED",

            "CANCELLED, PENDING",
            "CANCELLED, ENROLLED",
            "CANCELLED, REJECTED",
            "CANCELLED, CANCELLED",
            "CANCELLED, DROPPED",

            "DROPPED, PENDING",
            "DROPPED, ENROLLED",
            "DROPPED, REJECTED",
            "DROPPED, CANCELLED",
            "DROPPED, DROPPED",
        })
        void shouldDenyEnrollmentTransition(EnrollmentStatus from , EnrollmentStatus to) {
            var enrollment = EnrollmentEntity.builder()
                .status(from)
                .build();

            assertFalse(enrollment.canTransitionTo(to));
        }
    }
}
