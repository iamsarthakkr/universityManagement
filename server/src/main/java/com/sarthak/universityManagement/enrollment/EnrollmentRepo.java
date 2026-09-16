package com.sarthak.universityManagement.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepo extends JpaRepository<EnrollmentEntity, Integer> {
    boolean existsBy_StudentId_And_CourseOfferingId(Integer studentId, Integer courseOfferingId);
}
