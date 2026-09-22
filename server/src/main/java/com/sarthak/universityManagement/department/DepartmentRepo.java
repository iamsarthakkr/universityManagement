package com.sarthak.universityManagement.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepo extends JpaRepository<DepartmentEntity, Integer> {
    Optional<DepartmentEntity> findByCode(String code);

    List<DepartmentEntity> findAllByOrderByNameAsc();
}
