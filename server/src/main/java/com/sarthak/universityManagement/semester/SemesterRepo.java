package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.types.SemesterTerm;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepo extends JpaRepository<SemesterEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SemesterEntity> findForUpdateById(int semesterId);

    List<SemesterEntity> findAllByOrderByYearAscTermDesc();

    Optional<SemesterEntity> findByTermAndYear(SemesterTerm term, Integer year);

    boolean existsByTermAndYear(SemesterTerm term, Integer year);
}
