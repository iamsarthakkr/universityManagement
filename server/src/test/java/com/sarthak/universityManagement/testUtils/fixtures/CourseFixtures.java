package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;

public class CourseFixtures {

    public static CourseEntity.CourseEntityBuilder course(InstructorEntity instructor, DepartmentEntity department) {
        return CourseEntity.builder()
                .code("test-code")
                .title("test title")
                .description("test description")
                .credits(3)
                .capacity(30)
                .active(true)
                .department(department)
                .instructor(instructor);
    }

    public static CourseRequest.CourseRequestBuilder courseRequest(Integer instructorId, Integer departmentId) {
        return CourseRequest.builder()
                .code("test-code")
                .title("test title")
                .description("test description")
                .credits(3)
                .capacity(30)
                .departmentId(departmentId)
                .instructorId(instructorId);
    }
}
