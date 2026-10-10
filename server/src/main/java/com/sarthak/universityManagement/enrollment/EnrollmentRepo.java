package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.LockModeType;
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

    @EntityGraph(attributePaths = {
        "student",
        "courseOffering.course.department",
        "courseOffering.instructor.department",
        "courseOffering.semester"
    })
    @Query("""
        SELECT e
        FROM EnrollmentEntity e
        WHERE (e.student.id = :studentId)
            AND (:semesterId IS NULL OR e.courseOffering.semester.id = :semesterId)
            AND (:enrollmentStatus IS NULL OR e.status = :enrollmentStatus)
        ORDER BY e.courseOffering.semester.year DESC, e.courseOffering.semester.term DESC,
            e.courseOffering.course.department.name ASC, e.courseOffering.course.code ASC,
            e.courseOffering.section ASC,
            e.createdAt DESC, e.id DESC
    """)
    List<EnrollmentEntity> findByStudentId(Integer studentId, @Nullable Integer semesterId, @Nullable EnrollmentStatus enrollmentStatus);

    @EntityGraph(attributePaths = {
        "student",
        "courseOffering.course.department",
        "courseOffering.instructor.department",
        "courseOffering.semester"
    })
    @Query("""
        SELECT e
        FROM EnrollmentEntity e
        WHERE e.courseOffering.id = :courseOfferingId
            AND (:status IS NULL OR e.status = :status)
        ORDER BY e.createdAt DESC, e.id DESC
    """)
    List<EnrollmentEntity> findAllForCourseOfferingWithDetails(Integer courseOfferingId, @Nullable EnrollmentStatus status);

    Optional<EnrollmentEntity> findByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByIdAndCourseOffering_Instructor_User_Id(Integer enrollmentId, Integer userId);

    boolean existsByIdAndStudent_User_Id(Integer enrollmentId, Integer userId);
}
