package com.sarthak.universityManagement.registration.instructor;

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

public interface InstructorRegistrationRepo extends JpaRepository<InstructorRegistrationEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InstructorRegistrationEntity> findForUpdateById(Integer id);

    @EntityGraph(attributePaths = "department")
    @Query("""
        SELECT ir
        FROM InstructorRegistrationEntity ir
        WHERE (:departmentId IS NULL OR ir.department.id = :departmentId)
            AND (:status IS NULL OR ir.registrationStatus = :status)
        ORDER BY ir.createdAt ASC, ir.id ASC
    """)
    List<InstructorRegistrationEntity> search(
        @Nullable Integer departmentId,
        @Nullable RegistrationStatus status
    );

    default List<InstructorRegistrationEntity> search(RegistrationFilter filter) {
        return search(filter.departmentId(), filter.status());
    }

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
