package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class CourseOfferingEntityTest {
    @Nested
    class Capacity {
        @ParameterizedTest
        @CsvSource({
            "10, 1, true",
            "10, 9, true",
            "10, 10, false"
        })
        void shouldDetermineEnrollmentStatus(int capacity, int enrolled, boolean status) {
            var offering = CourseOfferingEntity.builder()
                .capacity(capacity)
                .enrolled(enrolled)
                .build();

            assertEquals(status, offering.canEnroll());
        }
    }

    @Nested
    class Enrollment {
        @ParameterizedTest
        @CsvSource({
            "10, 0",
            "10, 9"
        })
        void shouldCorrectlyEnrollWhenSeatsAvailable(int capacity, int enrolled) {
            var offering = CourseOfferingEntity.builder()
                .capacity(capacity)
                .enrolled(enrolled)
                .build();

            offering.enroll();
            assertEquals(enrolled + 1, offering.getEnrolled());
        }

        @ParameterizedTest
        @CsvSource({
            "10, 10",
        })
        void shouldThrowWhenSeatsUnavailable(int capacity, int enrolled) {
            var offering = CourseOfferingEntity.builder()
                .capacity(capacity)
                .enrolled(enrolled)
                .build();

            assertThrows(BadRequestException.class, offering::enroll);
            assertEquals(enrolled, offering.getEnrolled());
        }

        @ParameterizedTest
        @CsvSource({
            "10, 1",
            "10, 10"
        })
        void shouldCorrectlyReleaseSeat(int capacity, int enrolled) {
            var offering = CourseOfferingEntity.builder()
                .capacity(capacity)
                .enrolled(enrolled)
                .build();

            offering.releaseEnrolled();
            assertEquals(enrolled - 1, offering.getEnrolled());
        }

        @ParameterizedTest
        @CsvSource({
            "10, 0",
        })
        void shouldThrowWhenNoSeatTaken(int capacity, int enrolled) {
            var offering = CourseOfferingEntity.builder()
                .capacity(capacity)
                .enrolled(enrolled)
                .build();

            assertThrows(BadRequestException.class, offering::releaseEnrolled);
            assertEquals(enrolled, offering.getEnrolled());
        }
    }
}
