package com.sarthak.universityEnrollmentManagement.repo;

import com.sarthak.universityEnrollmentManagement.entity.StudentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepo extends JpaRepository<StudentEntity, Integer> {
    Optional<StudentEntity> findByEmail(final String email);

    List<StudentEntity> findByFirstName(final String firstName);

    List<StudentEntity> findByLastName(final String lastName);

    List<StudentEntity> findByFirstNameAndLastName(final String firstName, final String lastName);
    
    boolean existsByEmail(String email);
    
    boolean existsByUserId(Integer userId);
}
