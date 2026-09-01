package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.testUtils.testConfigs.RegistrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(RegistrationTestConfig.class)
@Transactional
public class InstructorRegistrationServiceIntegrationTests {

    @Autowired
    private InstructorRegistrationService service;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private StudentRegistrationSeeder studentRegistrationSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private UserSeeder userSeeder;

    private InstructorRegistrationRequest.InstructorRegistrationRequestBuilder instructorRegistrationRequestBuilder;

    @BeforeEach
    void beforeEach() {
        var savedDepartment = departmentSeeder.saveDefault("test-department");
        instructorRegistrationRequestBuilder = InstructorRegistrationFixtures
                .instructorRegistrationRequest(savedDepartment.getId());
    }

    @Test
    void shouldCreateInstructorRegistrationSuccessfully() {
        var req = instructorRegistrationRequestBuilder.username("instructor1").email("instructor1@example.com").build();
        InstructorRegistrationResponse resp = service.createRegistration(req);

        assertNotNull(resp);
        assertEquals("instructor1", resp.username());
        assertEquals("instructor1@example.com", resp.email());

        var entity = instructorRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals("instructor1", entity.getFirstName());
        assertEquals("instructor1@example.com", entity.getLastName());
    }

    @Test
    void shouldCreateInstructorRegistrationWithPendingStatus() {
        var req = instructorRegistrationRequestBuilder.username("instructor2").email("instructor2@example.com").build();
        InstructorRegistrationResponse resp = service.createRegistration(req);

        var entity = instructorRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals(RegistrationStatus.PENDING, entity.getRegistrationStatus());
    }

    @Test
    void shouldRejectDuplicateUsernameInUserTable() {
        var user = UserFixtures.user().username("duplicate").email("user-1@abc").role(Role.INSTRUCTOR).build();
        userSeeder.saveUser(user);

        var req = instructorRegistrationRequestBuilder.username("duplicate").email("user-2@abc").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldRejectDuplicateEmailInUserTable() {
        var user = UserFixtures.user().username("user-1").email("duplicate@abc").role(Role.INSTRUCTOR).build();
        userSeeder.saveUser(user);

        var req = instructorRegistrationRequestBuilder.username("user-2").email("duplicate@abc").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldRejectDuplicateUsernameInInstructorRegistrationTable() {
        var req1 = instructorRegistrationRequestBuilder.username("instructor3").email("instructor3a@example.com").build();
        service.createRegistration(req1);

        var req2 = instructorRegistrationRequestBuilder.username("instructor3").email("instructor3b@example.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }

    @Test
    void shouldRejectDuplicateEmailInInstructorRegistrationTable() {
        var req1 = instructorRegistrationRequestBuilder.username("instructor4a").email("instructor4@example.com").build();
        service.createRegistration(req1);

        var req2 = instructorRegistrationRequestBuilder.username("instructor4b").email("instructor4@example.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }

    @Test
    void shouldRejectDuplicateUsernameInStudentRegistrationTable() {
        studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("student1").email("unique@email.com").build()
        );
        var req = instructorRegistrationRequestBuilder.username("student1").email("instructor5@example.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldRejectDuplicateEmailInStudentRegistrationTable() {
        studentRegistrationSeeder.saveStudentRegistration(
                StudentRegistrationFixtures.studentRegistration().username("uniqueuser").email("dupe@email.com").build()
        );
        var req = instructorRegistrationRequestBuilder.username("instructor6").email("dupe@email.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldPersistInstructorRegistration() {
        var req = instructorRegistrationRequestBuilder.username("instructor7").email("instructor7@example.com").build();
        InstructorRegistrationResponse resp = service.createRegistration(req);

        var entity = instructorRegistrationRepo.findById(resp.id());
        assertTrue(entity.isPresent());
        assertEquals("instructor7", entity.get().getUsername());
    }
}