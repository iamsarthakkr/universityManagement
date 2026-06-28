package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.testUtils.TestDataSetup;
import com.sarthak.universityManagement.testUtils.TestSecurityUtils;
import com.sarthak.universityManagement.user.UserEntity;
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
@Import(TestDataSetup.class)
@Transactional
public class CourseServiceAuthorizationTests {

    @Autowired
    private CourseService courseService;
    @Autowired
    private TestDataSetup setup;

    @AfterEach
    void cleanup() {
        TestSecurityUtils.clearAuthentication();
    }

    private void setupAdmin() {
        UserEntity user = setup.savedUser("admin", "admin@example.com", Role.ADMIN);
        TestSecurityUtils.authenticateAs(user);
    }

    private void setupStudent() {
        UserEntity user = setup.savedUser("student1", "student1@example.com", Role.STUDENT);
        TestSecurityUtils.authenticateAs(user);
    }

    private void setupInstructor() {
        UserEntity user = setup.savedUser("instructor1", "instructor1@example.com", Role.INSTRUCTOR);
        TestSecurityUtils.authenticateAs(user);
    }

    @Test
    void createCourse_whenAdmin_shouldAllow() {
        setupAdmin();
        InstructorEntity instructor = setup.savedInstructor("instructor2", "instructor2@example.com");
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, instructor.getId());

        assertDoesNotThrow(() -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenStudent_shouldDeny() {
        setupStudent();
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, 1);

        assertThrows(AuthorizationDeniedException.class, () -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenInstructor_shouldDeny() {
        setupInstructor();
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, 1);

        assertThrows(AuthorizationDeniedException.class, () -> courseService.createCourse(req));
    }

    @Test
    void createCourse_whenAnonymous_shouldDeny() {
        CourseRequest req = new CourseRequest("Computer Science", "CS101", "Intro to CS", "An intro course", 3, 30, 1);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> courseService.createCourse(req));
    }
}
