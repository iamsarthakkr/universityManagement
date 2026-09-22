package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepo extends JpaRepository<EnrollmentEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EnrollmentEntity> findForUpdateById(Integer id);

    @EntityGraph(attributePaths = {"student", "courseOffering"})
    List<EnrollmentEntity> findByStudentId(Integer studentId);

    @Query("""
        select distinct e
        from EnrollmentEntity e
        join fetch e.student s
        join fetch e.courseOffering co
        where co.id = :courseOfferingId
            and (:status is null or e.status = :status)
        order by e.createdAt DESC
    """)
    List<EnrollmentEntity> findAllForCourseOfferingWithDetails(Integer courseOfferingId, @Nullable EnrollmentStatus status);

    Optional<EnrollmentEntity> findByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByIdAndCourseOffering_Instructor_User_Id(Integer enrollmentId, Integer userId);

    boolean existsByIdAndStudent_User_Id(Integer enrollmentId, Integer userId);
}
