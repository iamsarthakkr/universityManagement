package com.sarthak.universityManagement.instructor;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.instructor.validators.InstructorValidator;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InstructorValidatorTests {

    @Test
    void shouldNotThrow_whenInstructorIsValidForCourse() {
        var department = DepartmentFixtures.departmentWithCode("dep1").build();
        var courseEntity = CourseFixtures.course(department).build();
        var instructorEntity = InstructorFixtures.instructor().department(department).build();

        assertDoesNotThrow(() -> InstructorValidator.validateInstructorForCourse(courseEntity, instructorEntity));
    }

    @Test
    void shouldThrow_whenInstructorIsNotValidForCourse() {
        var department1 = DepartmentFixtures.departmentWithCode("dep1").build();
        var department2 = DepartmentFixtures.departmentWithCode("dep2").build();
        var courseEntity = CourseFixtures.course(department1).build();
        var instructorEntity = InstructorFixtures.instructor().department(department2).build();

        assertThrows(BadRequestException.class, () -> InstructorValidator.validateInstructorForCourse(courseEntity, instructorEntity));
    }

}
