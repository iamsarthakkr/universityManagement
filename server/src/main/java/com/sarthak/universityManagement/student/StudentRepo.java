package com.sarthak.universityManagement.student;

import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<StudentEntity, Integer> {
    @EntityGraph(attributePaths = "user")
    Optional<StudentEntity> findById(@Nonnull Integer userId);

    @EntityGraph(attributePaths = "user")
    Optional<StudentEntity> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    boolean existsByIdAndUser_Id(Integer id, Integer userId);
}
