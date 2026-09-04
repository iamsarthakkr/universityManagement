package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RegistrationTestConfig.class)
@Transactional
public class CourseServiceAuthorizationTests {

    @Autowired
    private CourseService courseService;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;


    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }

    private void setupUser(Role role) {
        var user = userSeeder.saveUser(
                UserFixtures.user().username("seeded-user").email("seeded@abc").role(role).build()
        );
        TestSecurityUtils.authenticateAs(user);
    }

    @Test
    void createCourse_whenAdmin_shouldAllow() {
        setupUser(Role.ADMIN);
        var department = departmentSeeder.saveDefault("cs-1");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, instructor.getId());

        assertDoesNotThrow(() -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenStudent_shouldDeny() {
        setupUser(Role.STUDENT);
        var department = departmentSeeder.saveDefault("cs-1");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, instructor.getId());

        assertThrows(AuthorizationDeniedException.class, () -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenInstructor_shouldDeny() {
        setupUser(Role.INSTRUCTOR);
        var department = departmentSeeder.saveDefault("cs-1");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, instructor.getId());

        assertThrows(AuthorizationDeniedException.class, () -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenAnonymous_shouldDeny() {
        var department = departmentSeeder.saveDefault("cs-1");
        InstructorEntity instructor = instructorSeeder.saveDefaultInstructorWithDepartment(department);
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, instructor.getId());

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> courseService.createCourse(req));
    }
}
