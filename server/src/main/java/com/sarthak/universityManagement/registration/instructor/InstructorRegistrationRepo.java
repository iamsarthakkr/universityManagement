package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructorRegistrationRepo extends JpaRepository<InstructorRegistrationEntity, Integer> {
    List<InstructorRegistrationEntity> findByRegistrationStatus(RegistrationStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
