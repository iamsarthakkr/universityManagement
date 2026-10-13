package com.sarthak.universityManagement.instructor;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.dto.InstructorFilter;
import com.sarthak.universityManagement.instructor.dto.InstructorResponse;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorFixtures;
import com.sarthak.universityManagement.testUtils.security.AuthOutcome;
import com.sarthak.universityManagement.testUtils.security.RoleActor;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
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

public class InstructorServiceIntegrationTests extends IntegrationTests {

    private static final InstructorFilter NO_FILTER = new InstructorFilter(null);

    @Autowired
    private InstructorService instructorService;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    @AfterEach
    void tearDown() {
        TestAuthentication.clear();
    }

    private InstructorEntity saveInstructor(DepartmentEntity department, String firstName) {
        return instructorSeeder.saveInstructor(
            InstructorFixtures.instructorWithDepartment(department)
                .firstName(firstName)
                .user(userSeeder.saveDefault(Role.INSTRUCTOR))
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
        void shouldAuthorizeListingInstructors(RoleActor actor, AuthOutcome outcome) {
            instructorSeeder.saveDefaultInstructor(departmentSeeder.saveDefault("dep"));
            actor.authenticate();

            if(outcome == AuthOutcome.ALLOWED) {
                assertEquals(1, instructorService.getInstructors(NO_FILTER).size());
            } else {
                assertThrows(outcome.expectedException(), () -> instructorService.getInstructors(NO_FILTER));
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
        void shouldReturnEmptyListWhenNoInstructorsExist() {
            assertTrue(instructorService.getInstructors(NO_FILTER).isEmpty());
        }

        @Test
        void shouldReturnInstructorsFromEveryDepartmentWhenFilterIsEmpty() {
            var maths = departmentSeeder.saveDefault("maths");
            var cs = departmentSeeder.saveDefault("cse");
            saveInstructor(maths, "Asha");
            saveInstructor(cs, "Bilal");

            var instructors = instructorService.getInstructors(NO_FILTER);

            assertEquals(
                Set.of(maths.getId(), cs.getId()),
                instructors.stream().map(s -> s.department().id()).collect(Collectors.toSet())
            );
        }

        @Test
        void shouldReturnOnlyInstructorsOfFilteredDepartment() {
            var maths = departmentSeeder.saveDefault("maths");
            var cs = departmentSeeder.saveDefault("cse");
            saveInstructor(maths, "Asha");
            var csInstructor = saveInstructor(cs, "Bilal");

            var instructors = instructorService.getInstructors(new InstructorFilter(cs.getId()));

            assertEquals(List.of(csInstructor.getId()), instructors.stream().map(InstructorResponse::id).toList());
        }

        @Test
        void shouldReturnNothingForUnknownDepartment() {
            saveInstructor(departmentSeeder.saveDefault("cse"), "Asha");

            assertTrue(instructorService.getInstructors(new InstructorFilter(Integer.MAX_VALUE)).isEmpty());
        }

        @Test
        void shouldOrderByDepartmentNameThenFirstName() {
            var maths = departmentSeeder.save(DepartmentFixtures.departmentWithCode("maths").name("Mathematics").build());
            var cs = departmentSeeder.save(DepartmentFixtures.departmentWithCode("cse").name("Computer Science").build());
            saveInstructor(maths, "Asha");
            saveInstructor(cs, "Chen");
            saveInstructor(cs, "Bilal");

            var names = instructorService.getInstructors(NO_FILTER).stream().map(InstructorResponse::firstName).toList();

            assertEquals(List.of("Bilal", "Chen", "Asha"), names);
        }

        @Test
        void shouldIncludeEmailAndDepartment() {
            var department = departmentSeeder.saveDefault("cse");
            var instructor = saveInstructor(department, "Asha");

            var response = instructorService.getInstructors(NO_FILTER).getFirst();

            assertEquals(instructor.getUser().getEmail(), response.email());
            assertEquals(department.getId(), response.department().id());
        }
    }
}
