package com.sarthak.universityManagement.courseOffering;

import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseOfferingRepo extends JpaRepository<CourseOfferingEntity, Integer> {

    @EntityGraph(attributePaths = {"course", "instructor", "semester"})
    Optional<CourseOfferingEntity> findById(@NonNull Integer id);

    @EntityGraph(attributePaths = {"course", "instructor", "semester"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CourseOfferingEntity> findForUpdateById(Integer id);

    @EntityGraph(attributePaths = {"course", "instructor", "semester"})
    Optional<CourseOfferingEntity> findByCourseIdAndSemesterIdAndSection(Integer courseId, Integer semesterId, String section);

    boolean existsByCourseIdAndSemesterIdAndSection(Integer courseId, Integer semesterId, String section);

    boolean existsByIdAndInstructor_User_Id(Integer id, Integer instructorUserId);

    @EntityGraph(attributePaths = {
        "course.department",
        "instructor.department",
        "semester"
    })
    @Query("""
        SELECT co
        FROM CourseOfferingEntity co
        WHERE (:semesterId IS NULL OR co.semester.id = :semesterId)
            AND (:instructorUserId IS NULL OR co.instructor.user.id = :instructorUserId)
        ORDER BY co.semester.year ASC, co.semester.term ASC,
                co.course.department.name ASC, co.course.code ASC,
                co.section ASC, co.id ASC
    """)
    List<CourseOfferingEntity> findManagedOfferings(Integer semesterId, Integer instructorUserId);
}
