package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class MeIntegrationTests extends IntegrationTests {
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private StudentSeeder studentSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    @Test
    void adminShouldHaveNoProfile() {
        var admin = userSeeder.saveDefault(Role.ADMIN);
        TestAuthentication.asUser(admin);

        var me = currentUserService.getMe(currentUserService.getCurrentUserPrincipal());

        assertEquals(admin.getId(), me.id());
        assertEquals(admin.getUsername(), me.username());
        assertEquals(admin.getEmail(), me.email());
        assertEquals(Role.ADMIN, me.role());
        assertNull(me.profile());
    }

    @Test
    void studentShouldHaveStudentProfile() {
        var department = departmentSeeder.saveDefault("dep1");
        var student = studentSeeder.saveDefault(department);
        TestAuthentication.asStudent(student);

        var me = currentUserService.getMe(currentUserService.getCurrentUserPrincipal());

        assertEquals(student.getUser().getId(), me.id());
        assertEquals(student.getUser().getEmail(), me.email());
        assertEquals(Role.STUDENT, me.role());
        // profile id is the student id, not the user id: it's what student-scoped endpoints take
        assertEquals(student.getId(), me.profile().id());
        assertEquals(student.getFirstName(), me.profile().firstName());
        assertEquals(student.getLastName(), me.profile().lastName());
        assertEquals(department.getId(), me.profile().department().id());
    }

    @Test
    void instructorShouldHaveInstructorProfile() {
        var department = departmentSeeder.saveDefault("dep1");
        var instructor = instructorSeeder.saveDefaultInstructor(department);
        TestAuthentication.asInstructor(instructor);

        var me = currentUserService.getMe(currentUserService.getCurrentUserPrincipal());

        assertEquals(instructor.getUser().getId(), me.id());
        assertEquals(instructor.getUser().getEmail(), me.email());
        assertEquals(Role.INSTRUCTOR, me.role());
        assertEquals(instructor.getId(), me.profile().id());
        assertEquals(instructor.getFirstName(), me.profile().firstName());
        assertEquals(instructor.getLastName(), me.profile().lastName());
        assertEquals(department.getId(), me.profile().department().id());
    }

    @Test
    void anonymousShouldBeRejected() {
        RoleActor.ANONYMOUS.authenticate();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> currentUserService.getMe(currentUserService.getCurrentUserPrincipal()));
    }
}
