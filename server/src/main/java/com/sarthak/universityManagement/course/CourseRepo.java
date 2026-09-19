package com.sarthak.universityManagement.course;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<CourseEntity, Integer> {
    @EntityGraph(attributePaths = "department")
    Optional<CourseEntity> findByCode(String code);

    @EntityGraph(attributePaths = "department")
    List<CourseEntity> findAllByOrderByDepartmentNameAsc();
}
