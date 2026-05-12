package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.user.dto.CreateUserCommand;
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
