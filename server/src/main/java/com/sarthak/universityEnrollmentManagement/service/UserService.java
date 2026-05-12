package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.internal.CreateUserCommand;
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
    public UserEntity createUserForRegistration(CreateUserCommand createUserCommand) {
        if(userRepo.existsByUsername(createUserCommand.username())) {
            throw new ConflictException("User already present with username " + createUserCommand.username());
        }
        
        if(userRepo.existsByEmail(createUserCommand.email())) {
            throw new ConflictException("User already present with email " + createUserCommand.email());
        }
        
        UserEntity newUser = UserMapper.toEntity(createUserCommand);
        newUser.setActive(true);
        return userRepo.save(newUser);
    }
}
