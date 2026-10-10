package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.config.RepoTests;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CourseRepoTests extends RepoTests {

    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    @Test
    void shouldRejectDuplicateCode() {
        var department = departmentSeeder.saveDefault("dep1");
        courseRepo.saveAndFlush(CourseFixtures.course(department).code("CS101").build());

        assertThrows(DataIntegrityViolationException.class, () -> courseRepo.saveAndFlush(
            CourseFixtures.course(department).code("CS101").title("another title").build()
        ));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10})
    void shouldRejectCreditsOutOfRange(int credits) {
        var department = departmentSeeder.saveDefault("dep1");

        assertThrows(DataIntegrityViolationException.class, () -> courseRepo.saveAndFlush(
            CourseFixtures.course(department).credits(credits).build()
        ));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 9})
    void shouldAcceptCreditsAtRangeBoundaries(int credits) {
        var department = departmentSeeder.saveDefault("dep1");

        assertDoesNotThrow(() -> courseRepo.saveAndFlush(
            CourseFixtures.course(department).credits(credits).build()
        ));
    }

    @Test
    void shouldFindAndCheckExistenceByCode() {
        var department = departmentSeeder.saveDefault("dep1");
        var saved = courseRepo.saveAndFlush(CourseFixtures.course(department).code("CS101").build());

        assertTrue(courseRepo.existsByCode("CS101"));
        assertEquals(saved.getId(), courseRepo.findByCode("CS101").orElseThrow().getId());

        assertFalse(courseRepo.existsByCode("CS102"));
        assertTrue(courseRepo.findByCode("CS102").isEmpty());
    }

    @Test
    void shouldOrderByDepartmentNameThenCode() {
        var maths = departmentSeeder.save(DepartmentFixtures.departmentWithCode("maths").name("Mathematics").build());
        var cs = departmentSeeder.save(DepartmentFixtures.departmentWithCode("cse").name("Computer Science").build());

        courseRepo.saveAndFlush(CourseFixtures.course(maths).code("MT101").build());
        courseRepo.saveAndFlush(CourseFixtures.course(cs).code("CS200").build());
        courseRepo.saveAndFlush(CourseFixtures.course(cs).code("CS100").build());

        var codes = courseRepo.findAllByOrderByDepartmentNameAscCodeAsc()
            .stream()
            .map(CourseEntity::getCode)
            .toList();

        assertEquals(List.of("CS100", "CS200", "MT101"), codes);
    }
}
