package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({RegistrationTestConfig.class, CourseSeeder.class})
@Transactional
public class CourseServiceIntegrationTests {

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

            assertNotNull(courseRepo.findById(resp.courseId()));
        }

        @Test
        void shouldThrowWhenDepartmentDoesNotExist() {
            CourseRequest req = CourseFixtures.courseRequest(9999).build();

            assertThrows(ResourceNotFoundException.class, () -> courseService.createCourse(req));
        }

    }

    @Nested
    class CatalogueTests {
        @Test
        void shouldReturnEmptyCatalogueWhenNoCoursesExist() {
            List<CourseCatalogueResponse> catalogue = courseService.getCoursesCatalogue();

            assertNotNull(catalogue);
            assertTrue(catalogue.isEmpty());
        }

        @Test
        void shouldReturnCatalogueGroupedByDepartment() {
            var department1 = departmentSeeder.save(DepartmentFixtures.departmentWithCode("cse").name("Computer Science").build());
            var department2 = departmentSeeder.save(DepartmentFixtures.departmentWithCode("maths").name("Mathematics").build());

            courseSeeder.save(CourseFixtures.course(department1).code("CS100").build());
            courseSeeder.save(CourseFixtures.course(department1).code("CS101").build());
            courseSeeder.save(CourseFixtures.course(department2).code("MT101").build());

            List<CourseCatalogueResponse> catalogue = courseService.getCoursesCatalogue();

            assertNotNull(catalogue);
            assertEquals(2, catalogue.size());

            CourseCatalogueResponse csDept = catalogue.stream()
                    .filter(c -> c.departmentName().equals("Computer Science"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(2, csDept.courseList().size());

            CourseCatalogueResponse mathDept = catalogue.stream()
                    .filter(c -> c.departmentName().equals("Mathematics"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(1, mathDept.courseList().size());
        }
    }

}
