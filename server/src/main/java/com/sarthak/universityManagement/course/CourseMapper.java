package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.department.DepartmentMapper;

public class CourseMapper {
    public static CourseEntity toEntity(CourseRequest courseRequest) {
        return CourseEntity
            .builder()
            .code(courseRequest.code())
            .title(courseRequest.title())
            .description(courseRequest.description())
            .credits(courseRequest.credits())
            .build();
    }

    public static CourseResponse toResponse(CourseEntity courseEntity) {
        return CourseResponse
            .builder()
            .id(courseEntity.getId())
            .department(DepartmentMapper.toResponse(courseEntity.getDepartment()))
            .code(courseEntity.getCode())
            .title(courseEntity.getTitle())
            .description(courseEntity.getDescription())
            .credits(courseEntity.getCredits())
            .build();
    }
}
