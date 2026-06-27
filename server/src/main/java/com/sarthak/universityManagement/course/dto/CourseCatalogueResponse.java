package com.sarthak.universityManagement.course.dto;

import java.util.List;

public record CourseCatalogueResponse(
    String department,
    List<CourseResponse> courseList
) {}
