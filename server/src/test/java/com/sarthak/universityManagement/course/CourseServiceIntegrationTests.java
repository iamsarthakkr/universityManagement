package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
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
@Import(RegistrationTestConfig.class)
@Transactional
public class CourseServiceIntegrationTests {

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private TestDataSetup setup;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    @BeforeEach
    void setupAdmin() {
        var user = userSeeder.saveDefaultUser(Role.ADMIN);
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

        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, instructor.getId());
        CourseResponse resp = courseService.createCourse(req);

        assertNotNull(resp);
        assertEquals("CS101", resp.code());
        assertEquals("Intro to CS", resp.title());
        assertEquals("Computer Science", resp.department());
        assertEquals(3, resp.credits());
        assertEquals(30, resp.capacity());
        assertEquals(instructor.getId(), resp.instructorId());
    }

    @Test
    void shouldPersistCourseAfterCreation() {
        var department = departmentSeeder.saveDefault("test-department");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseRequest req = new CourseRequest("Mathematics", "MATH101", "Calculus I", "Differential calculus", 4, 25, instructor.getId());

        CourseResponse resp = courseService.createCourse(req);

        var saved = courseRepo.findById(resp.courseId());
        assertTrue(saved.isPresent());
        assertEquals("MATH101", saved.get().getCode());
    }

    @Test
    void shouldThrowWhenInstructorDoesNotExist() {
        CourseRequest req = new CourseRequest("Physics", "PHY101", "Mechanics", "Classical mechanics", 3, 20, 99999);

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
        var department = departmentSeeder.saveDefault("test-department");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);

        setup.savedCourse(instructor, "CS101");
        setup.savedCourse(instructor, "CS102");

        InstructorEntity instructor2 = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseEntity mathCourse = courseRepo.saveAndFlush(
            CourseEntity.builder()
                .department("Mathematics")
                .code("MATH101")
                .title("Calculus")
                .description("Differential calculus")
                .credits(4)
                .capacity(25)
                .active(true)
                .instructor(instructor2)
                .build()
        );

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
        CourseRequest req = new CourseRequest("Computer Science", "CS201", "Data Structures", "Trees and graphs", 3, 30, instructor.getId());

        CourseResponse resp = courseService.createCourse(req);

        assertEquals(instructor.getFirstName(), resp.instructor());
    }
}
