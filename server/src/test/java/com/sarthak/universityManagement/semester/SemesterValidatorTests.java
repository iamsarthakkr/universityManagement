package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.validators.SemesterValidator;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SemesterValidatorTests {

    @ParameterizedTest
    @CsvSource({
        "PLANNED"
    })
    void shouldAllowOffering(SemesterStatus status) {
        var entity = SemesterFixtures.semester().status(status).build();

        assertDoesNotThrow(() -> SemesterValidator.validateSemesterAllowsOfferings(entity));
    }

    @ParameterizedTest
    @CsvSource({
        "ACTIVE",
        "COMPLETED",
        "CANCELLED"
    })
    void shouldNotAllowOffering(SemesterStatus status) {
        var entity = SemesterFixtures.semester().status(status).build();

        assertThrows(BadRequestException.class, () -> SemesterValidator.validateSemesterAllowsOfferings(entity));
    }

}
