package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.instructor.InstructorEntity;

public class CourseFixtures {

    public static CourseEntity.CourseEntityBuilder course(InstructorEntity instructor) {
        return CourseEntity.builder()
                .department("test-department")
                .code("test-code")
                .title("test title")
                .description("test description")
                .credits(3)
                .capacity(30)
                .active(true)
                .instructor(instructor);
    }

    public static CourseRequest.CourseRequestBuilder courseRequest(Integer instructorId) {
        return CourseRequest.builder()
                .department("test-department")
                .code("test-code")
                .title("test title")
                .description("test description")
                .credits(3)
                .capacity(30)
                .instructorId(instructorId);
    }
}
