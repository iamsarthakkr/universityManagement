package com.sarthak.universityManagement.courseOffering;

import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    boolean existsByCourseIdAndSemesterIdAndSection(Integer courseId, Integer semesterId, String section);

    boolean existsByIdAndInstructor_User_Id(Integer id, Integer instructorUserId);

    List<CourseOfferingEntity> findAllBySemesterId(Integer semesterId);
}
