package com.sarthak.universityManagement.registration.validators;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.department.DepartmentService;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final DepartmentService departmentService;
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

    public void validateDepartmentAvailable(Integer departmentId) {
        if(!departmentService.existsById(departmentId)) {
            throw new ConflictException("Department with id " + departmentId + " does not exist");
        }
    }
    
}
