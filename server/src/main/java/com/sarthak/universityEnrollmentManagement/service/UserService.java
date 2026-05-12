package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import com.sarthak.universityEnrollmentManagement.exceptions.ConflictException;
import com.sarthak.universityEnrollmentManagement.mapper.UserMapper;
import com.sarthak.universityEnrollmentManagement.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    
    @Transactional
    public UserEntity createUserForRegistration(StudentRegistrationEntity registration) {
        if(userRepo.existsByUsername(registration.getUsername())) {
            throw new ConflictException("User already present with username " + registration.getUsername());
        }
        
        if(userRepo.existsByEmail(registration.getEmail())) {
            throw new ConflictException("User already present with email " + registration.getEmail());
        }
        
        UserEntity newUser = UserMapper.toEntityFromStudentRegistration(registration);
        newUser.setActive(true);
        return userRepo.save(newUser);
    }
}
