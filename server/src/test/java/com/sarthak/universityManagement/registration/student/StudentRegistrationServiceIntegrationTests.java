package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.testUtils.fixtures.InstructorRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorRegistrationSeeder;
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
public class StudentRegistrationServiceIntegrationTests {

    @Autowired
    private StudentRegistrationService service;
    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;
    @Autowired
    private InstructorRegistrationSeeder instructorRegistrationSeeder;
    @Autowired
    private UserSeeder userSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    private DepartmentEntity department;
    private StudentRegistrationRequest.StudentRegistrationRequestBuilder studentRegistrationRequestBuilder;
    private InstructorRegistrationEntity.InstructorRegistrationEntityBuilder instructorRegistrationEntityBuilder;

    @BeforeEach
    public void setup() {
        department = departmentSeeder.saveDefault("test-department");
        studentRegistrationRequestBuilder = StudentRegistrationFixtures.studentRegistrationRequest(department.getId());
        instructorRegistrationEntityBuilder = InstructorRegistrationFixtures.instructorRegistration().department(department);
    }

    @Test
    void shouldCreateStudentRegistrationSuccessfully() {
        var req = studentRegistrationRequestBuilder.username("student1").email("student1@example.com").build();
        StudentRegistrationResponse resp = service.createRegistration(req);

        assertNotNull(resp);
        assertEquals("student1", resp.username());
        assertEquals("student1@example.com", resp.email());

        var entity = studentRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals("student1", entity.getUsername());
        assertEquals("student1@example.com", entity.getEmail());
    }

    @Test
    void shouldCreateStudentRegistrationWithPendingStatus() {
        var req = studentRegistrationRequestBuilder.username("student2").email("student2@example.com").build();
        StudentRegistrationResponse resp = service.createRegistration(req);

        var entity = studentRegistrationRepo.findById(resp.id()).orElseThrow();
        assertEquals(RegistrationStatus.PENDING, entity.getRegistrationStatus());
    }

    @Test
    void shouldRejectDuplicateUsernameInUserTable() {
        var entity = UserFixtures
                .user()
                .username("duplicate")
                .email("u-1@abc")
                .build();
        userSeeder.saveUser(entity);

        var req = studentRegistrationRequestBuilder.username("duplicate").email("u-2@abc").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldRejectDuplicateEmailInUserTable() {
        var entity = UserFixtures
                .user()
                .username("u-1")
                .email("duplicate@abc")
                .build();
        userSeeder.saveUser(entity);

        var req = studentRegistrationRequestBuilder.username("u-2").email("duplicate@abc").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldRejectDuplicateUsernameInStudentRegistrationTable() {
        var req1 = studentRegistrationRequestBuilder.username("student3").email("student3a@example.com").build();
        service.createRegistration(req1);

        var req2 = studentRegistrationRequestBuilder.username("student3").email("student3b@example.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }

    @Test
    void shouldRejectDuplicateEmailInStudentRegistrationTable() {
        var req1 = studentRegistrationRequestBuilder.username("student4a").email("student4@example.com").build();
        service.createRegistration(req1);

        var req2 = studentRegistrationRequestBuilder.username("student4b").email("student4@example.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req2));
    }

    @Test
    void shouldRejectDuplicateUsernameInInstructorRegistrationTable() {
        instructorRegistrationSeeder.saveInstructorRegistration(
                instructorRegistrationEntityBuilder.username("instructor1").email("unique@email.com").build()
        );
        var req = studentRegistrationRequestBuilder.username("instructor1").email("student5@example.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldRejectDuplicateEmailInInstructorRegistrationTable() {
        instructorRegistrationSeeder.saveInstructorRegistration(
                instructorRegistrationEntityBuilder.username("uniqueuser").email("dupe@email.com").build()
        );
        var req = studentRegistrationRequestBuilder.username("student6").email("dupe@email.com").build();
        assertThrows(ConflictException.class, () -> service.createRegistration(req));
    }

    @Test
    void shouldPersistStudentRegistration() {
        var req = studentRegistrationRequestBuilder.username("student7").email("student7@example.com").build();
        StudentRegistrationResponse resp = service.createRegistration(req);

        var entity = studentRegistrationRepo.findById(resp.id());
        assertTrue(entity.isPresent());
        assertEquals("student7", entity.get().getUsername());
    }

}
