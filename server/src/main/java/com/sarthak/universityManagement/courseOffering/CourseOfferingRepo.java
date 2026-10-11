package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingFilter;
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
public interface CourseOfferingRepo extends JpaRepository<CourseOfferingEntity, Integer> {

    @EntityGraph(attributePaths = {
        "course.department",
        "instructor.department",
        "semester"
    })
    Optional<CourseOfferingEntity> findById(Integer id);

    @EntityGraph(attributePaths = {
        "course.department",
        "instructor.department",
        "semester"
    })
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
            AND (:departmentId IS NULL OR co.course.department.id = :departmentId)
            AND (:instructorId IS NULL OR co.instructor.id = :instructorId)
        ORDER BY co.semester.year ASC, co.semester.term ASC,
                co.course.department.name ASC, co.course.code ASC,
                co.section ASC, co.id ASC
    """)
    List<CourseOfferingEntity> search(
        @Nullable Integer semesterId,
        @Nullable Integer departmentId,
        @Nullable Integer instructorId
    );

    default List<CourseOfferingEntity> search(CourseOfferingFilter filter) {
        return search(filter.semesterId(), filter.departmentId(), filter.instructorId());
    }

    default List<CourseOfferingEntity> searchForInstructor(Integer instructorId, CourseOfferingFilter filter) {
        return search(filter.semesterId(), filter.departmentId(), instructorId);
    }
}
