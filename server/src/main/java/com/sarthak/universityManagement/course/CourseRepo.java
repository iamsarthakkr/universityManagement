package com.sarthak.universityManagement.course;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<CourseEntity, Integer> {
    @EntityGraph(attributePaths = {"instructor", "department"})
    List<CourseEntity> findAllByOrderByDepartmentNameAsc();
}
