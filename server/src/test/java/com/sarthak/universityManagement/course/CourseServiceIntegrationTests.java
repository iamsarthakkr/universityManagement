package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import jdk.jfr.Frequency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void shouldCreateCourseSuccessfully() {
        var department = departmentSeeder.saveDefault("test-department");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);

        CourseRequest req = CourseFixtures.courseRequest(instructor.getId()).build();
        CourseResponse resp = courseService.createCourse(req);

        assertNotNull(resp);
        assertEquals(req.code(), resp.code());
        assertEquals(req.title(), resp.title());
        assertEquals(req.department(), resp.department());
        assertEquals(req.credits(), resp.credits());
        assertEquals(req.capacity(), resp.capacity());
        assertEquals(instructor.getId(), resp.instructorId());
    }

    @Test
    void shouldPersistCourseAfterCreation() {
        var department = departmentSeeder.saveDefault("test-department");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseRequest req = CourseFixtures.courseRequest(instructor.getId()).build();

        CourseResponse resp = courseService.createCourse(req);
        var saved = courseRepo.findById(resp.courseId());

        assertTrue(saved.isPresent());
        assertEquals(req.code(), saved.get().getCode());
    }

    @Test
    void shouldThrowWhenInstructorDoesNotExist() {
        CourseRequest req = CourseFixtures.courseRequest(9999).build();

        assertThrows(BadRequestException.class, () -> courseService.createCourse(req));
    }

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

        var instructor1 = InstructorFixtures.instructor().firstName("name-1").department(department1).build();
        var instructor2 = InstructorFixtures.instructor().firstName("name-2").department(department2).build();

        instructorSeeder.saveInstructor(instructor1);
        instructorSeeder.saveInstructor(instructor2);

        courseSeeder.save(CourseFixtures.course(instructor1).department(department1.getName()).code("CS100").build());
        courseSeeder.save(CourseFixtures.course(instructor1).department(department1.getName()).code("CS101").build());
        courseSeeder.save(CourseFixtures.course(instructor2).department(department2.getName()).code("MT101").build());

        List<CourseCatalogueResponse> catalogue = courseService.getCoursesCatalogue();

        assertNotNull(catalogue);
        assertEquals(2, catalogue.size());

        CourseCatalogueResponse csDept = catalogue.stream()
            .filter(c -> c.department().equals("Computer Science"))
            .findFirst()
            .orElseThrow();
        assertEquals(2, csDept.courseList().size());

        CourseCatalogueResponse mathDept = catalogue.stream()
            .filter(c -> c.department().equals("Mathematics"))
            .findFirst()
            .orElseThrow();
        assertEquals(1, mathDept.courseList().size());
    }

    @Test
    void shouldIncludeInstructorNameInCourseResponse() {
        var department = departmentSeeder.saveDefault("test-department");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);

        var req =  CourseFixtures.courseRequest(instructor.getId()).build();
        CourseResponse resp = courseService.createCourse(req);

        assertEquals(instructor.getFirstName(), resp.instructor());
    }
}
