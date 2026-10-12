package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class CourseOfferingEntityTest {

    private static CourseOfferingEntity offering(int capacity, int enrolled) {
        return CourseOfferingEntity.builder()
            .capacity(capacity)
            .enrolled(enrolled)
            .build();
    }

    @Test
    void shouldStartWithNoSeatsTakenWhenBuilt() {
        var offering = CourseOfferingEntity.builder().capacity(10).build();

        assertEquals(0, offering.getEnrolled());
        assertTrue(offering.canEnroll());
    }

    @Nested
    class Capacity {
        @ParameterizedTest(name = "{1}/{0} -> {2}")
        @CsvSource({
            "10, 0, true",
            "10, 1, true",
            "10, 9, true",
            "10, 10, false"
        })
        void shouldDetermineWhetherSeatsRemain(int capacity, int enrolled, boolean expected) {
            assertEquals(expected, offering(capacity, enrolled).canEnroll());
        }
    }

    @Nested
    class Seats {
        @ParameterizedTest(name = "{1}/{0}")
        @CsvSource({
            "10, 0",
            "10, 9"
        })
        void shouldTakeSeatWhenAvailable(int capacity, int enrolled) {
            var offering = offering(capacity, enrolled);

            offering.enroll();

            assertEquals(enrolled + 1, offering.getEnrolled());
        }

        @ParameterizedTest(name = "{1}/{0}")
        @CsvSource({
            "10, 10",
            "1, 1"
        })
        void shouldConflictWhenFull(int capacity, int enrolled) {
            var offering = offering(capacity, enrolled);

            assertThrows(ConflictException.class, offering::enroll);
            assertEquals(enrolled, offering.getEnrolled());
        }

        @ParameterizedTest(name = "{1}/{0}")
        @CsvSource({
            "10, 1",
            "10, 10"
        })
        void shouldReleaseSeat(int capacity, int enrolled) {
            var offering = offering(capacity, enrolled);

            offering.releaseEnrolled();

            assertEquals(enrolled - 1, offering.getEnrolled());
        }

        @Test
        void shouldConflictWhenReleasingWithNoSeatsTaken() {
            var offering = offering(10, 0);

            assertThrows(ConflictException.class, offering::releaseEnrolled);
            assertEquals(0, offering.getEnrolled());
        }
    }
}
