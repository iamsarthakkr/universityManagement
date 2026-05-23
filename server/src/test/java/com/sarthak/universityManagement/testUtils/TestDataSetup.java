package com.sarthak.universityManagement.testUtils;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationEntity;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
@Import(TestDataFactory.class)
public class TestDataSetup {
    private final UserRepo userRepo;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final TestDataFactory dataFactory;
    
    @Autowired
    public TestDataSetup(
        UserRepo userRepo,
        StudentRegistrationRepo studentRegistrationRepo,
        InstructorRegistrationRepo instructorRegistrationRepo,
        TestDataFactory dataFactory
    ) {
        this.userRepo = userRepo;
        this.studentRegistrationRepo = studentRegistrationRepo;
        this.instructorRegistrationRepo = instructorRegistrationRepo;
        this.dataFactory = dataFactory;
    }
    
    public UserEntity savedUser(String username, String email, Role role) {
        return userRepo.saveAndFlush(dataFactory.user(username, email, role));
    }
    public UserEntity savedUser(String username, String email, Role role, String password) {
        return userRepo.saveAndFlush(dataFactory.user(username, email, role, password));
    }
    
    public StudentRegistrationEntity savedStudentRegistration(String username, String email) {
        return studentRegistrationRepo.saveAndFlush(
            dataFactory.studentRegistrationEntity(username, email)
        );
    }
    public StudentRegistrationEntity savedStudentRegistration(String username, String email, String password) {
        return studentRegistrationRepo.saveAndFlush(
            dataFactory.studentRegistrationEntity(username, email, password)
        );
    }
    
    public InstructorRegistrationEntity savedInstructorRegistration(String username, String email) {
        return instructorRegistrationRepo.saveAndFlush(
            dataFactory.instructorRegistrationEntity(username, email)
        );
    }
    public InstructorRegistrationEntity savedInstructorRegistration(String username, String email, String password) {
        return instructorRegistrationRepo.saveAndFlush(
            dataFactory.instructorRegistrationEntity(username, email, password)
        );
    }
}
