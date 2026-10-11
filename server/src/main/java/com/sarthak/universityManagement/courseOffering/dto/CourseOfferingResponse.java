package com.sarthak.universityManagement.courseOffering.dto;

import com.sarthak.universityManagement.course.dto.CourseSummary;
import com.sarthak.universityManagement.courseOffering.types.CourseOfferingAction;
import com.sarthak.universityManagement.instructor.dto.InstructorSummary;
import com.sarthak.universityManagement.semester.dto.SemesterSummary;
import lombok.Builder;

import java.util.List;

@Builder
public record CourseOfferingResponse(
    Integer id,
    CourseSummary course,
    InstructorSummary instructor,
    SemesterSummary semester,
    String section,
    Integer capacity,
    Integer enrolled,
    Integer seatsAvailable,
    List<CourseOfferingAction> allowedActions
) { }
