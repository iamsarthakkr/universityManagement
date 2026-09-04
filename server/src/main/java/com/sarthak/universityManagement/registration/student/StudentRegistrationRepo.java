package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.types.RegistrationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRegistrationRepo extends JpaRepository<StudentRegistrationEntity, Integer> {
    @EntityGraph(attributePaths = "department")
    List<StudentRegistrationEntity> findByRegistrationStatus(RegistrationStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
