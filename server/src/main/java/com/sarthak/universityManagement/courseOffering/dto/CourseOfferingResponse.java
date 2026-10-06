package com.sarthak.universityManagement.courseOffering.dto;

import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.instructor.dto.InstructorResponse;
import com.sarthak.universityManagement.semester.dto.SemesterSummaryResponse;
import lombok.Builder;

@Builder
public record CourseOfferingResponse(
    int id,
    CourseResponse course,
    InstructorResponse instructor,
    SemesterSummaryResponse semester,
    String section,
    int capacity,
    int enrolled
) {
}
