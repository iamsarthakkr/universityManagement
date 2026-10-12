package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.student.dto.StudentFilter;
import com.sarthak.universityManagement.student.dto.StudentResponse;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.StudentFixtures;
import com.sarthak.universityManagement.testUtils.security.AuthOutcome;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StudentServiceIntegrationTests extends IntegrationTests {

    private static final StudentFilter NO_FILTER = new StudentFilter(null);

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentSeeder studentSeeder;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    private StudentEntity saveStudent(DepartmentEntity department, String firstName) {
        return studentSeeder.save(
            StudentFixtures.student(department)
                .firstName(firstName)
                .user(userSeeder.saveDefault(Role.STUDENT))
                .build()
        );
    }

    @Nested
    class ListAuthorization {

        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.of(RoleActor.ADMIN, AuthOutcome.ALLOWED),
                Arguments.of(RoleActor.INSTRUCTOR, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.STUDENT, AuthOutcome.ACCESS_DENIED),
                Arguments.of(RoleActor.ANONYMOUS, AuthOutcome.UNAUTHENTICATED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("cases")
        void shouldAuthorizeListingStudents(RoleActor actor, AuthOutcome outcome) {
            studentSeeder.saveDefault(departmentSeeder.saveDefault("dep"));
            actor.authenticate();

            if(outcome == AuthOutcome.ALLOWED) {
                assertEquals(1, studentService.getStudents(NO_FILTER).size());
            } else {
                assertThrows(outcome.expectedException(), () -> studentService.getStudents(NO_FILTER));
            }
        }
    }

    @Nested
    class ListTests {

        @BeforeEach
        void asAdmin() {
            RoleActor.ADMIN.authenticate();
        }

        @Test
        void shouldReturnEmptyListWhenNoStudentsExist() {
            assertTrue(studentService.getStudents(NO_FILTER).isEmpty());
        }

        @Test
        void shouldReturnStudentsFromEveryDepartmentWhenFilterIsEmpty() {
            var maths = departmentSeeder.saveDefault("maths");
            var cs = departmentSeeder.saveDefault("cse");
            saveStudent(maths, "Asha");
            saveStudent(cs, "Bilal");

            var students = studentService.getStudents(NO_FILTER);

            assertEquals(
                Set.of(maths.getId(), cs.getId()),
                students.stream().map(s -> s.department().id()).collect(Collectors.toSet())
            );
        }

        @Test
        void shouldReturnOnlyStudentsOfFilteredDepartment() {
            var maths = departmentSeeder.saveDefault("maths");
            var cs = departmentSeeder.saveDefault("cse");
            saveStudent(maths, "Asha");
            var csStudent = saveStudent(cs, "Bilal");

            var students = studentService.getStudents(new StudentFilter(cs.getId()));

            assertEquals(List.of(csStudent.getId()), students.stream().map(StudentResponse::id).toList());
        }

        @Test
        void shouldReturnNothingForUnknownDepartment() {
            saveStudent(departmentSeeder.saveDefault("cse"), "Asha");

            assertTrue(studentService.getStudents(new StudentFilter(Integer.MAX_VALUE)).isEmpty());
        }

        @Test
        void shouldOrderByDepartmentNameThenFirstName() {
            var maths = departmentSeeder.save(DepartmentFixtures.departmentWithCode("maths").name("Mathematics").build());
            var cs = departmentSeeder.save(DepartmentFixtures.departmentWithCode("cse").name("Computer Science").build());
            saveStudent(maths, "Asha");
            saveStudent(cs, "Chen");
            saveStudent(cs, "Bilal");

            var names = studentService.getStudents(NO_FILTER).stream().map(StudentResponse::firstName).toList();

            assertEquals(List.of("Bilal", "Chen", "Asha"), names);
        }

        @Test
        void shouldIncludeEmailAndDepartment() {
            var department = departmentSeeder.saveDefault("cse");
            var student = saveStudent(department, "Asha");

            var response = studentService.getStudents(NO_FILTER).getFirst();

            assertEquals(student.getUser().getEmail(), response.email());
            assertEquals(department.getId(), response.department().id());
        }
    }
}
