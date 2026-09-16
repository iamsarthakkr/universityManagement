package com.sarthak.universityManagement.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepo extends JpaRepository<EnrollmentEntity, Integer> {
    boolean existsByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);
}
