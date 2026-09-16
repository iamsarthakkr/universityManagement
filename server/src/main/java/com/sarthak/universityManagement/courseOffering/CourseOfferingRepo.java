package com.sarthak.universityManagement.courseOffering;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseOfferingRepo extends JpaRepository<CourseOfferingEntity, Integer> {
    boolean existsByCourseIdAndSemesterIdAndSection(Integer courseId, Integer semesterId, String section);
    List<CourseOfferingEntity> findAllBySemesterId(Integer semesterId);
}
