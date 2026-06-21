package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;

public class CourseMapper {
    public static CourseEntity toEntity(CourseRequest courseRequest) {
        return CourseEntity
            .builder()
            .department(courseRequest.department())
            .code(courseRequest.code())
            .title(courseRequest.title())
            .description(courseRequest.description())
            .credits(courseRequest.credits())
            .capacity(courseRequest.capacity())
            .build();
    }
    public static CourseResponse toResponse(CourseEntity courseEntity) {
        return CourseResponse
            .builder()
            .courseId(courseEntity.getId())
            .department(courseEntity.getDepartment())
            .code(courseEntity.getCode())
            .title(courseEntity.getTitle())
            .description(courseEntity.getDescription())
            .credits(courseEntity.getCredits())
            .capacity(courseEntity.getCapacity())
            .instructorId(courseEntity.getInstructor().getId())
            .instructor(courseEntity.getInstructor().getFirstName())
            .build();
    }
}
