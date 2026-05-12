package com.sarthak.universityEnrollmentManagement.repo;

import com.sarthak.universityEnrollmentManagement.entity.StudentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepo extends JpaRepository<StudentEntity, Integer> {
    boolean existsByUserId(Integer userId);
}
