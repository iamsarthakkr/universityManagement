package com.sarthak.universityEnrollmentManagement.repo;

import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<UserEntity, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
