package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public final class UserSeeder {
    private final UserRepo userRepo;

    @Autowired
    public UserSeeder(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public UserEntity saveUser(UserEntity userEntity) {
        return userRepo.saveAndFlush(userEntity);
    }

    public UserEntity saveDefaultUser(Role role) {
        return saveUser(
                UserFixtures
                        .user()
                        .role(role)
                        .build()
        );
    }
}
