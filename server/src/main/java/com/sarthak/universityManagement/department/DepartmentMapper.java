package com.sarthak.universityManagement.department;

import com.sarthak.universityManagement.department.dto.DepartmentResponse;

import java.util.List;

public class DepartmentMapper {

    public static DepartmentResponse toResponse(DepartmentEntity entity) {
        return new DepartmentResponse(entity.getId(), entity.getName(), entity.getCode());
    }

    public static List<DepartmentResponse> toResponseList(List<DepartmentEntity> entities) {
        return entities.stream().map(DepartmentMapper::toResponse).toList();
    }
}
