package com.sarthak.universityManagement.instructor;

import com.sarthak.universityManagement.instructor.dto.InstructorFilter;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepo extends JpaRepository<InstructorEntity, Integer> {
    @EntityGraph(attributePaths = {"user", "department"})
    @Query("""
        SELECT i
        FROM InstructorEntity i
        WHERE (:departmentId IS NULL OR i.department.id = :departmentId)
        ORDER BY i.department.name ASC, i.firstName ASC, i.id ASC
    """)
    List<InstructorEntity> search(@Nullable Integer departmentId);

    default List<InstructorEntity> search(InstructorFilter filter) {
        return search(filter.departmentId());
    }

    @EntityGraph(attributePaths = "department")
    Optional<InstructorEntity> findByUserId(Integer userId);

    Optional<InstructorEntity> findByUser_Username(String username);

    boolean existsByUserId(Integer userId);
}
