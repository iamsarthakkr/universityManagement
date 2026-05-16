package com.sarthak.universityManagement.testUtils;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.user.UserEntity;

import java.time.LocalDate;

public final class TestDataFactory {
    private TestDataFactory() {}
    
    public static UserEntity user(String username, String email, Role role) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secret");
        user.setRole(role);
        user.setActive(true);
        return user;
    }
    
    public static StudentRegistrationRequest studentRegistrationRequest(String username, String email) {
        return new StudentRegistrationRequest(
            username,
            "secret",
            "John",
            "Doe",
            email,
            "1234567890",
            LocalDate.of(2000, 1, 1),
            "123 Main St",
            "Father",
            "Mother"
        );
    }
    
    public static StudentRegistrationEntity studentRegistrationEntity(String username, String email) {
        return StudentRegistrationEntity.builder()
            .registrationStatus(RegistrationStatus.PENDING)
            .username(username)
            .password("secret")
            .firstName("John")
            .lastName("Doe")
            .email(email)
            .phoneNumber("1234567890")
            .dateOfBirth(LocalDate.of(2000, 1, 1))
            .address("123 Main St")
            .fatherName("Father")
            .motherName("Mother")
            .build();
    }
    
    public static InstructorRegistrationRequest instructorRegistrationRequest(String username, String email) {
        return new InstructorRegistrationRequest(
            username,
            "secret",
            "Jane",
            "Doe",
            email,
            "1234567890",
            "Computer Science"
        );
    }
    
    public static InstructorRegistrationEntity instructorRegistrationEntity(String username, String email) {
        return InstructorRegistrationEntity.builder()
            .registrationStatus(RegistrationStatus.PENDING)
            .username(username)
            .password("secret")
            .firstName("Jane")
            .lastName("Doe")
            .email(email)
            .phoneNumber("1234567890")
            .department("Computer Science")
            .build();
    }
}
