package com.sarthak.universityManagement.instructor;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepo extends JpaRepository<InstructorEntity, Integer> {
    @EntityGraph(attributePaths = "user")
    List<InstructorEntity> findAll();
    Optional<InstructorEntity> findByUserId(Integer userId);

    Optional<InstructorEntity> findByUser_Username(String username);

    boolean existsByUserId(Integer userId);
}
