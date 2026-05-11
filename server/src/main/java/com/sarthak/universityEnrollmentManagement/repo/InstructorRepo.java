package com.sarthak.universityEnrollmentManagement.repo;

import com.sarthak.universityEnrollmentManagement.entity.InstructorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorRepo extends JpaRepository<InstructorEntity, Integer> {
    boolean existsByEmail(String email);
}
