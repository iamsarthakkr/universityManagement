package com.sarthak.universityManagement.course.dto;

import java.util.List;

public record CourseCatalogueResponse(
    Integer departmentId,
    String departmentName,
    List<CourseResponse> courseList
) {}
