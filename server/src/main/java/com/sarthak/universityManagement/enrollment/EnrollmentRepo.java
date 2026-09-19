package com.sarthak.universityManagement.enrollment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepo extends JpaRepository<EnrollmentEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EnrollmentEntity> findForUpdateById(Integer id);

    List<EnrollmentEntity> findByStudentId(Integer studentId);

    List<EnrollmentEntity> findByCourseOfferingId(Integer courseOfferingId);

    Optional<EnrollmentEntity> findByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByIdAndCourseOffering_Instructor_User_Id(Integer enrollmentId, Integer userId);

    boolean existsByIdAndStudent_User_Id(Integer enrollmentId, Integer userId);
}
