package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.testUtils.security.WithInstructor;
import com.sarthak.universityManagement.testUtils.security.WithStudent;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.*;

public class CourseOfferingAuthorizationTests extends IntegrationTests {
    @Autowired
    private CourseOfferingService courseOfferingService;

    @Autowired
    private CourseSeeder courseSeeder;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    @Nested
    class Creation {

        CreateCourseOfferingRequest courseOfferingRequest;

        @BeforeEach
        void setUp() {
            var dep = departmentSeeder.saveDefault("dep");
            var course = courseSeeder.saveDefault(dep);
            var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor = instructorSeeder.saveDefaultInstructor(dep);

            courseOfferingRequest = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(semester.getId())
                .instructorId(instructor.getId())
                .build();
        }

        @Test
        @WithAdmin
        void adminShouldAllow() {
            var ret = courseOfferingService.createOffering(courseOfferingRequest);
            assertNotNull(ret);
        }

        @Test
        @WithStudent
        void studentShouldDeny() {
            assertThrows(AuthorizationDeniedException.class, () -> courseOfferingService.createOffering(courseOfferingRequest));
        }

        @Test
        @WithInstructor
        void instructorShouldDeny() {
            assertThrows(AuthorizationDeniedException.class, () -> courseOfferingService.createOffering(courseOfferingRequest));
        }

        @Test
        void anonymousShouldDeny() {
            assertThrows(AuthenticationCredentialsNotFoundException.class, () -> courseOfferingService.createOffering(courseOfferingRequest));
        }
    }

}
