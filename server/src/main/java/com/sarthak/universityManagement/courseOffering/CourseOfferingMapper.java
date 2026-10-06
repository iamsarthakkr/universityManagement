package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.course.CourseMapper;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.instructor.InstructorMapper;
import com.sarthak.universityManagement.semester.SemesterMapper;

import java.time.LocalDate;

public final class CourseOfferingMapper {
    public static CourseOfferingEntity toEntity(CreateCourseOfferingRequest request) {
        return CourseOfferingEntity.builder()
            .capacity(request.capacity())
            .section(request.section())
            .enrolled(0)
            .build();
    }

    public static CourseOfferingResponse toResponse(CourseOfferingEntity entity, LocalDate today) {
        return CourseOfferingResponse.builder()
            .id(entity.getId())
            .course(CourseMapper.toResponse(entity.getCourse()))
            .instructor(InstructorMapper.toResponse(entity.getInstructor()))
            .semester(SemesterMapper.toSummary(entity.getSemester(), today))
            .capacity(entity.getCapacity())
            .enrolled(entity.getEnrolled())
            .section(entity.getSection())
            .build();
    }
}
