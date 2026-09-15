package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.semester.SemesterEntity;

public final class CourseOfferingFixtures {

    public static CourseOfferingEntity.CourseOfferingEntityBuilder courseOffering(
        CourseEntity courseEntity, InstructorEntity instructorEntity, SemesterEntity semesterEntity
    ) {
        return CourseOfferingEntity.builder()
            .section("A")
            .capacity(100)
            .course(courseEntity)
            .instructor(instructorEntity)
            .semester(semesterEntity);
    }

    public static CreateCourseOfferingRequest.CreateCourseOfferingRequestBuilder courseOfferingRequest() {
        return CreateCourseOfferingRequest.builder()
            .section("A")
            .capacity(100);
    }

}
