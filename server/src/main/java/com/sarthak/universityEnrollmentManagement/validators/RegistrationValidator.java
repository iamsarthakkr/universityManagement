package com.sarthak.universityEnrollmentManagement.validators;

import com.sarthak.universityEnrollmentManagement.exceptions.ConflictException;
import com.sarthak.universityEnrollmentManagement.repo.InstructorRegistrationRepo;
import com.sarthak.universityEnrollmentManagement.repo.StudentRegistrationRepo;
import com.sarthak.universityEnrollmentManagement.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final UserRepo userRepo;
    
    public void validateEmailAvailable(String email) {
        if(studentRegistrationRepo.existsByEmail(email)) {
            throw new ConflictException("Registration request already present for student with email " + email);
        }
        
        if(instructorRegistrationRepo.existsByEmail(email)) {
            throw new ConflictException("Registration request already present for instructor with email " + email);
        }
        
        if(userRepo.existsByEmail(email)) {
            throw new ConflictException("User already registered with email " + email);
        }
    }
    
    public void validateUserNameAvailable(String username) {
        if(studentRegistrationRepo.existsByUsername(username)) {
            throw new ConflictException("Registration request already present for student with username " + username);
        }
        
        if(instructorRegistrationRepo.existsByUsername(username)) {
            throw new ConflictException("Registration request already present for instructor with username " + username);
        }
        
        if(userRepo.existsByUsername(username)) {
            throw new ConflictException("User already registered with username " + username);
        }
    }
    
}
