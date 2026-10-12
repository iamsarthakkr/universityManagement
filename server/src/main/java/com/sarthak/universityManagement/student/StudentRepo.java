package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.student.dto.StudentFilter;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<StudentEntity, Integer> {
    @EntityGraph(attributePaths = "department")
    Optional<StudentEntity> findByUserId(Integer userId);

    Optional<StudentEntity> findByUser_Username(String username);

    @EntityGraph(attributePaths = {"user", "department"})
    @Query("""
        SELECT s
        FROM StudentEntity s
        WHERE (:departmentId IS NULL OR s.department.id = :departmentId)
        ORDER BY s.department.name ASC, s.firstName ASC, s.id
    """)
    List<StudentEntity> search(@Nullable Integer departmentId);

    default List<StudentEntity> search(StudentFilter filter) {
        return search(filter.departmentId());
    }

    boolean existsByUserId(Integer userId);

    boolean existsByIdAndUser_Id(Integer id, Integer userId);
}
