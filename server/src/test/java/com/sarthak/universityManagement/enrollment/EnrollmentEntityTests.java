package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.user.UserEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Nested
    class Ownership {
        private static final int STUDENT_USER_ID = 10;
        private static final int INSTRUCTOR_USER_ID = 20;
        private static final int UNRELATED_USER_ID = 30;

        private final EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .student(StudentEntity.builder()
                .user(UserEntity.builder().id(STUDENT_USER_ID).build())
                .build())
            .courseOffering(CourseOfferingEntity.builder()
                .instructor(InstructorEntity.builder()
                    .user(UserEntity.builder().id(INSTRUCTOR_USER_ID).build())
                    .build())
                .build())
            .status(EnrollmentStatus.PENDING)
            .build();

        @Test
        void shouldBelongToStudentUser() {
            assertTrue(enrollment.belongsToUser(STUDENT_USER_ID));
        }

        @ParameterizedTest
        @ValueSource(ints = {INSTRUCTOR_USER_ID, UNRELATED_USER_ID})
        void shouldNotBelongToOtherUsers(int userId) {
            assertFalse(enrollment.belongsToUser(userId));
        }

        @Test
        void shouldBeTaughtByOfferingInstructorUser() {
            assertTrue(enrollment.isTaughtBy(INSTRUCTOR_USER_ID));
        }

        @ParameterizedTest
        @ValueSource(ints = {STUDENT_USER_ID, UNRELATED_USER_ID})
        void shouldNotBeTaughtByOtherUsers(int userId) {
            assertFalse(enrollment.isTaughtBy(userId));
        }
    }
}
