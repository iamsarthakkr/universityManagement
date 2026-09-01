package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;

import java.time.LocalDate;

public final class StudentRegistrationFixtures {

    public static StudentRegistrationEntity.StudentRegistrationEntityBuilder studentRegistration() {
        return StudentRegistrationEntity.builder()
                .registrationStatus(RegistrationStatus.PENDING)
                .username("test-student")
                .password("test-password")
                .firstName("test-first")
                .lastName("test-last")
                .email("test@test.com")
                .dateOfBirth(LocalDate.of(2000, 1, 1));
    }

    public static StudentRegistrationRequest.StudentRegistrationRequestBuilder studentRegistrationRequest() {
        return StudentRegistrationRequest.builder()
                .username("test-student")
                .password("test-password")
                .firstName("test-first")
                .lastName("test-last")
                .email("test@test.com")
                .dateOfBirth(LocalDate.of(2000, 1, 1));
    }

}
