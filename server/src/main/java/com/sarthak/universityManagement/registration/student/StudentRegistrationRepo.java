package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRegistrationRepo extends JpaRepository<StudentRegistrationEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StudentRegistrationEntity> findForUpdateById(Integer id);

    @EntityGraph(attributePaths = "department")
    @Query("""
        SELECT sr
        FROM StudentRegistrationEntity sr
        WHERE (:departmentId IS NULL OR sr.department.id = :departmentId)
            AND (:status IS NULL OR sr.registrationStatus = :status)
        ORDER BY sr.createdAt ASC, sr.id ASC
    """)
    List<StudentRegistrationEntity> search(
        @Nullable Integer departmentId,
        @Nullable RegistrationStatus status
    );

    default List<StudentRegistrationEntity> search(RegistrationFilter filter) {
        return search(filter.departmentId(), filter.status());
    }

    boolean existsByUsernameAndRegistrationStatus(String username, RegistrationStatus status);

    boolean existsByEmailAndRegistrationStatus(String email, RegistrationStatus status);
}
