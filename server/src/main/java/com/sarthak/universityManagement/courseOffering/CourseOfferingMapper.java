package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;

public final class CourseOfferingMapper {
    public static CourseOfferingEntity toEntity(CreateCourseOfferingRequest request) {
        return CourseOfferingEntity.builder()
            .capacity(request.capacity())
            .section(request.section())
            .build();
    }

    public static CourseOfferingResponse toResponse(CourseOfferingEntity entity) {
        return CourseOfferingResponse.builder()
            .id(entity.getId())
            .courseId(entity.getCourse().getId())
            .instructorId(entity.getInstructor().getId())
            .semesterId(entity.getSemester().getId())
            .capacity(entity.getCapacity())
            .section(entity.getSection())
            .build();
    }
}
