package com.sarthak.universityManagement.testUtils;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.AppSecurityBeansConfig;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Import(AppSecurityBeansConfig.class)
public final class TestDataFactory {
    private final PasswordEncoder passwordEncoder;
    private final String defaultEmail = "test@abc.com";
    private final String defaultPassword = "secret";
    
    @Autowired
    public TestDataFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserEntity user(String username, String email, Role role) { return user(username, email, role, defaultPassword); }
    public UserEntity user(String username, String email, Role role, String password) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(getEncodedPassword(password));
        user.setRole(role);
        user.setActive(true);
        return user;
    }
    
    public UserPrincipal userPrincipal(String username,  String password) {
        return userPrincipal(username, defaultEmail, Role.ADMIN, password);
    }
    public UserPrincipal userPrincipal(String username, String email, Role role, String password) {
        return new UserPrincipal(1, username, getEncodedPassword(password), role, true);
    }
    
    public StudentRegistrationRequest studentRegistrationRequest(String username, String email) { return studentRegistrationRequest(username, email, defaultPassword); }
    public StudentRegistrationRequest studentRegistrationRequest(String username, String email, String password) {
        return new StudentRegistrationRequest(
            username,
            getEncodedPassword(password),
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
    
    public StudentRegistrationEntity studentRegistrationEntity(String username, String email) { return studentRegistrationEntity(username, email, defaultPassword); }
    public StudentRegistrationEntity studentRegistrationEntity(String username, String email, String password) {
        return StudentRegistrationEntity.builder()
            .registrationStatus(RegistrationStatus.PENDING)
            .username(username)
            .password(getEncodedPassword(password))
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
    
    public InstructorRegistrationRequest instructorRegistrationRequest(String username, String email) { return instructorRegistrationRequest(username, email, defaultPassword); }
    public InstructorRegistrationRequest instructorRegistrationRequest(String username, String email, String password) {
        return new InstructorRegistrationRequest(
            username,
            getEncodedPassword(password),
            "Jane",
            "Doe",
            email,
            "1234567890",
            "Computer Science"
        );
    }
    
    public InstructorRegistrationEntity instructorRegistrationEntity(String username, String email) { return instructorRegistrationEntity(username, email, defaultPassword); }
    public InstructorRegistrationEntity instructorRegistrationEntity(String username, String email, String password) {
        return InstructorRegistrationEntity.builder()
            .registrationStatus(RegistrationStatus.PENDING)
            .username(username)
            .password(getEncodedPassword(password))
            .firstName("Jane")
            .lastName("Doe")
            .email(email)
            .phoneNumber("1234567890")
            .department("Computer Science")
            .build();
    }
    
    public String getDefaultPassword() {
        return defaultPassword;
    }
    
    private String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
