package com.sarthak.universityManagement.admin;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AdminUser adminUser;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if(userRepo.existsByUsername(adminUser.getUsername())) {
            return;
        }
        
        UserEntity admin = UserEntity
            .builder()
            .username(adminUser.getUsername())
            .password(passwordEncoder.encode(adminUser.getPassword()))
            .email(adminUser.getEmail())
            .role(Role.ADMIN)
            .active(true)
            .build();
        
        userRepo.save(admin);
    }
}
