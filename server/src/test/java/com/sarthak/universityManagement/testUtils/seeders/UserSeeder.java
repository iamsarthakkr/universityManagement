package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

@TestComponent
@ActiveProfiles("test")
public final class UserSeeder {
    private final UserRepo userRepo;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    public UserSeeder(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public UserEntity saveUser(UserEntity userEntity) {
        return userRepo.saveAndFlush(userEntity);
    }

    public UserEntity saveDefault(Role role) {
        var curr = counter.incrementAndGet();
        return saveUser(
            UserFixtures
                .user()
                .username("seed-user-" +  curr)
                .email("seedUser" + curr + "@abc")
                .role(role)
                .build()
        );
    }

    public UserEntity seedOrGet(Role role, String username) {
        var existing = userRepo.findByUsername(username);
        return existing.orElseGet(() -> saveUser(
            UserFixtures.user()
                .username(username)
                .email(username + "@abc")
                .role(role)
                .build()
        ));
    }
}
