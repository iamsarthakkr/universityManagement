package com.sarthak.universityManagement.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepo extends JpaRepository<EnrollmentEntity, Integer> {
    List<EnrollmentEntity> findByStudentId(Integer studentId);

    List<EnrollmentEntity> finByCourseOffering_Id(Integer courseOfferingId);

    boolean existsByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByIdAndCourseOffering_Instructor_User_Id(Integer enrollmentId, Integer userId);

    boolean existsByIdAndStudent_User_Id(Integer enrollmentId, Integer userId);
}
