package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CourseServiceIntegrationTests extends IntegrationTests {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private CourseSeeder courseSeeder;

    @BeforeEach
    void setupAdmin() {
        var user = userSeeder.saveUser(UserFixtures.user().username("seed-user").email("seed@abc").build());
        TestSecurityUtils.authenticateAs(user);
    }

    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }

    @Nested
    class CreationTests {
        @Test
        void shouldCreateCourseSuccessfully() {
            var department = departmentSeeder.saveDefault("dep-test");

            CourseRequest req = CourseFixtures.courseRequest(department.getId()).build();
            CourseResponse resp = courseService.createCourse(req);

            assertNotNull(resp);
            assertEquals(req.code(), resp.code());
            assertEquals(req.title(), resp.title());
            assertEquals(req.credits(), resp.credits());

            assertNotNull(courseRepo.findById(resp.id()));
        }

        @Test
        void shouldRejectDuplicateCode() {
            var department = departmentSeeder.saveDefault("dep-test");
            courseService.createCourse(CourseFixtures.courseRequest(department.getId()).code("CS101").build());

            var duplicate = CourseFixtures.courseRequest(department.getId()).code("CS101").title("another title").build();
            var ex = assertThrows(ConflictException.class, () -> courseService.createCourse(duplicate));

            assertEquals("Course with code CS101 already exists!", ex.getMessage());
            assertEquals(1, courseRepo.count());
        }

        @Test
        void shouldThrowWhenCourseDoesNotExist() {
            assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(Integer.MAX_VALUE));
        }

        @Test
        void shouldThrowWhenDepartmentDoesNotExist() {
            CourseRequest req = CourseFixtures.courseRequest(9999).build();

            assertThrows(ResourceNotFoundException.class, () -> courseService.createCourse(req));
        }

    }

    @Nested
    class ListTests {
        @Test
        void shouldReturnEmptyListWhenNoCoursesExist() {
            assertTrue(courseService.getCourses().isEmpty());
        }

        @Test
        void shouldReturnFlatListOrderedByDepartmentNameThenCode() {
            var maths = departmentSeeder.save(DepartmentFixtures.departmentWithCode("maths").name("Mathematics").build());
            var cs = departmentSeeder.save(DepartmentFixtures.departmentWithCode("cse").name("Computer Science").build());

            courseSeeder.save(CourseFixtures.course(maths).code("MT101").build());
            courseSeeder.save(CourseFixtures.course(cs).code("CS101").build());
            courseSeeder.save(CourseFixtures.course(cs).code("CS100").build());

            List<CourseResponse> courses = courseService.getCourses();

            assertEquals(List.of("CS100", "CS101", "MT101"), courses.stream().map(CourseResponse::code).toList());
            // Each course carries its department, so clients can group without another request
            assertEquals(List.of("Computer Science", "Computer Science", "Mathematics"),
                courses.stream().map(c -> c.department().name()).toList());
        }
    }

}
