package com.sarthak.universityManagement.department;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepo departmentRepo;

    @Autowired
    public DepartmentService(DepartmentRepo departmentRepo) {
        this.departmentRepo = departmentRepo;
    }

    public List<DepartmentResponse> getDepartments() {
        return DepartmentMapper.toResponseList(departmentRepo.findAllByOrderByNameAsc());
    }
}
