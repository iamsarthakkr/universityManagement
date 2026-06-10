package com.sarthak.universityManagement.instructor;

import com.sarthak.universityManagement.student.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepo extends JpaRepository<InstructorEntity, Integer> {
    Optional<InstructorEntity> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
}
