package com.sarthak.universityManagement.instructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorRepo extends JpaRepository<InstructorEntity, Integer> {
    boolean existsByUserId(Integer userId);
}
