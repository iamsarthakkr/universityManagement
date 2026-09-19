package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.types.SemesterTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SemesterRepo extends JpaRepository<SemesterEntity, Integer> {
    Optional<SemesterEntity> findByTermAndYear(SemesterTerm term, Integer year);
}
