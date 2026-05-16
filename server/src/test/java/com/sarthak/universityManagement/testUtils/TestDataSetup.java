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
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public class TestDataSetup {
    private final UserRepo userRepo;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    
    @Autowired
    public TestDataSetup(
        UserRepo userRepo,
        StudentRegistrationRepo studentRegistrationRepo,
        InstructorRegistrationRepo instructorRegistrationRepo
    ) {
        this.userRepo = userRepo;
        this.studentRegistrationRepo = studentRegistrationRepo;
        this.instructorRegistrationRepo = instructorRegistrationRepo;
    }
    
    
    public UserEntity savedUser(String username, String email, Role role) {
        return userRepo.saveAndFlush(TestDataFactory.user(username, email, role));
    }
    
    public StudentRegistrationEntity savedStudentRegistration(String username, String email) {
        return studentRegistrationRepo.saveAndFlush(
            TestDataFactory.studentRegistrationEntity(username, email)
        );
    }
    
    public InstructorRegistrationEntity savedInstructorRegistration(String username, String email) {
        return instructorRegistrationRepo.saveAndFlush(
            TestDataFactory.instructorRegistrationEntity(username, email)
        );
    }
}
