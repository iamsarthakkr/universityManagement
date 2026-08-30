package com.sarthak.universityManagement.department;

import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepo departmentRepo;

    @Autowired
    public DepartmentService(DepartmentRepo departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments() {
        return DepartmentMapper.toResponseList(departmentRepo.findAllByOrderByNameAsc());
    }

    public DepartmentEntity getDepartmentById(Integer departmentId) {
        return departmentRepo
                .findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id: " + departmentId + " not found!"));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer departmentId) {
        return departmentRepo.existsById(departmentId);
    }
}
