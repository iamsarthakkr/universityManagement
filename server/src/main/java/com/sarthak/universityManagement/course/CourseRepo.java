package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.course.dto.CourseFilter;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<CourseEntity, Integer> {
    @EntityGraph(attributePaths = "department")
    Optional<CourseEntity> findById(Integer id);

    @EntityGraph(attributePaths = "department")
    Optional<CourseEntity> findByCode(String code);

    @EntityGraph(attributePaths = "department")
    @Query("""
       SELECT c
       FROM CourseEntity c
       WHERE (:departmentId IS NULL OR c.department.id = :departmentId)
       ORDER BY c.department.name ASC, c.code
    """)
    List<CourseEntity> search(@Nullable Integer departmentId);

    default List<CourseEntity> search(CourseFilter filter) {
        return search(filter.departmentId());
    }

    boolean existsByCode(String code);
}
