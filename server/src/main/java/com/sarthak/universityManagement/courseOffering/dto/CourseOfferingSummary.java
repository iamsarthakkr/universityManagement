package com.sarthak.universityManagement.courseOffering.dto;

import com.sarthak.universityManagement.course.dto.CourseSummary;
import com.sarthak.universityManagement.instructor.dto.InstructorSummary;
import com.sarthak.universityManagement.semester.dto.SemesterSummary;
import lombok.Builder;

@Builder
public record CourseOfferingSummary(
    Integer id,
    CourseSummary course,
    InstructorSummary instructor,
    SemesterSummary semester,
    String section
) { }
