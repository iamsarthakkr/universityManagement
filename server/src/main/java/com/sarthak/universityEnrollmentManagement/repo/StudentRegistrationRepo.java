package com.sarthak.universityEnrollmentManagement.repo;

import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRegistrationRepo extends JpaRepository<StudentRegistrationEntity, Integer> {
    List<StudentRegistrationEntity> findByRegistrationStatus(RegistrationStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
