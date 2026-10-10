package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CourseRequestValidationTests {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() {
        FACTORY.close();
    }

    private static CourseRequest.CourseRequestBuilder valid() {
        return CourseFixtures.courseRequest(1);
    }

    private static Map<String, String> violations(CourseRequest request) {
        return VALIDATOR.validate(request).stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
            Arguments.of("code missing", valid().code(null).build(), "code", "code required"),
            Arguments.of("code blank", valid().code("  ").build(), "code", "code required"),
            Arguments.of("code too long", valid().code("x".repeat(11)).build(), "code", "code must be between 1 and 10 characters"),

            Arguments.of("title missing", valid().title(null).build(), "title", "title required"),
            Arguments.of("title blank", valid().title("").build(), "title", "title required"),
            Arguments.of("title too long", valid().title("x".repeat(101)).build(), "title", "title must be between 1 and 100 characters"),

            Arguments.of("description missing", valid().description(null).build(), "description", "description required"),
            Arguments.of("description too long", valid().description("x".repeat(256)).build(), "description", "description must be between 1 and 255 characters"),

            Arguments.of("credits missing", valid().credits(null).build(), "credits", "credits required"),
            Arguments.of("credits below range", valid().credits(0).build(), "credits", "credits must be between 1 and 9"),
            Arguments.of("credits above range", valid().credits(10).build(), "credits", "credits must be between 1 and 9"),

            Arguments.of("department missing", valid().departmentId(null).build(), "departmentId", "department required")
        );
    }

    @Test
    void shouldAcceptValidRequestIncludingBoundaries() {
        assertTrue(violations(valid().build()).isEmpty());
        assertTrue(violations(valid().code("x".repeat(10)).title("x".repeat(100)).description("x".repeat(255)).credits(1).build()).isEmpty());
        assertTrue(violations(valid().credits(9).build()).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCases")
    void shouldRejectWithFieldMessage(String label, CourseRequest request, String field, String expectedMessage) {
        assertEquals(Map.of(field, expectedMessage), violations(request));
    }
}
