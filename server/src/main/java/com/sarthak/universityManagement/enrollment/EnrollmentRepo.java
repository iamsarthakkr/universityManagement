package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.dto.EnrollmentFilter;
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
        WHERE (:studentId IS NULL OR e.student.id = :studentId)
            AND (:courseOfferingId IS NULL OR e.courseOffering.id = :courseOfferingId)
            AND (:semesterId IS NULL OR e.courseOffering.semester.id = :semesterId)
            AND (:status IS NULL OR e.status = :status)
        ORDER BY e.courseOffering.semester.year DESC, e.courseOffering.semester.term DESC,
            e.courseOffering.course.department.name ASC, e.courseOffering.course.code ASC,
            e.courseOffering.section ASC,
            e.createdAt DESC, e.id DESC
    """)
    List<EnrollmentEntity> search(
        @Nullable Integer studentId,
        @Nullable Integer courseOfferingId,
        @Nullable Integer semesterId,
        @Nullable EnrollmentStatus status
    );

    default List<EnrollmentEntity> searchForStudent(Integer studentId, EnrollmentFilter filter) {
        return search(studentId, null, filter.semesterId(), filter.status());
    }

    default List<EnrollmentEntity> searchForOffering(Integer offeringId, EnrollmentFilter filter) {
        // An offering belongs to one semester, so filter.semesterId() has nothing to narrow
        return search(null, offeringId, null, filter.status());
    }

    Optional<EnrollmentEntity> findByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByStudentIdAndCourseOfferingId(Integer studentId, Integer courseOfferingId);

    boolean existsByIdAndCourseOffering_Instructor_User_Id(Integer enrollmentId, Integer userId);

    boolean existsByIdAndStudent_User_Id(Integer enrollmentId, Integer userId);
}
