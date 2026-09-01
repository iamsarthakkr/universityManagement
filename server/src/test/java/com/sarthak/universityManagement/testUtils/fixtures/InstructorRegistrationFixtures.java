package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;

public class InstructorRegistrationFixtures {

    public static InstructorRegistrationEntity.InstructorRegistrationEntityBuilder instructorRegistration() {
        return InstructorRegistrationEntity.builder()
                .registrationStatus(RegistrationStatus.PENDING)
                .username("test-instructor")
                .password("test-password")
                .firstName("test-first")
                .lastName("test-last")
                .email("test@test.com");
    }

    public static InstructorRegistrationEntity.InstructorRegistrationEntityBuilder instructorRegistration(DepartmentEntity department) {
        return instructorRegistration().department(department);
    }

    public static InstructorRegistrationRequest.InstructorRegistrationRequestBuilder instructorRegistrationRequest(Integer departmentId) {
        return InstructorRegistrationRequest.builder()
                .username("test-instructor")
                .password("test-password")
                .firstName("test-first")
                .lastName("test-last")
                .email("test@test.com")
                .departmentId(departmentId);
    }
}
