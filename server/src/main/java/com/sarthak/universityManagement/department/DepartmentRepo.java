package com.sarthak.universityManagement.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepo extends JpaRepository<DepartmentEntity, Integer> {
    List<DepartmentEntity> findAllByOrderByNameAsc();
}
