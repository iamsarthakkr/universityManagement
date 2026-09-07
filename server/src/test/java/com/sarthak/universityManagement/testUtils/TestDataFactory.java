package com.sarthak.universityManagement.testUtils;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.AppSecurityBeansConfig;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.instructor.InstructorEntity;
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

    public String getDefaultPassword() {
        return defaultPassword;
    }
    
    public String getDefaultEmail() {
        return defaultEmail;
    }
    
    private String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
