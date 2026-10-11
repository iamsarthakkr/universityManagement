package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.course.CourseMapper;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingSummary;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;
import com.sarthak.universityManagement.courseOffering.types.CourseOfferingAction;
import com.sarthak.universityManagement.instructor.InstructorMapper;
import com.sarthak.universityManagement.semester.SemesterMapper;

import java.time.LocalDate;
import java.util.List;

public final class CourseOfferingMapper {
    public static CourseOfferingEntity toEntity(CreateCourseOfferingRequest request) {
        return CourseOfferingEntity.builder()
            .capacity(request.capacity())
            .section(request.section())
            .enrolled(0)
            .build();
    }

    public static CourseOfferingResponse toResponse(CourseOfferingEntity offering, LocalDate today, List<CourseOfferingAction> allowedActions) {
        return CourseOfferingResponse.builder()
            .id(offering.getId())
            .capacity(offering.getCapacity())
            .enrolled(offering.getEnrolled())
            .section(offering.getSection())
            .seatsAvailable(offering.seatsAvailable())
            .allowedActions(allowedActions)
            .course(CourseMapper.toSummary(offering.getCourse()))
            .instructor(InstructorMapper.toSummary(offering.getInstructor()))
            .semester(SemesterMapper.toSummary(offering.getSemester(), today))
            .build();
    }

    public static CourseOfferingSummary toSummary(CourseOfferingEntity offering, LocalDate today) {
        return CourseOfferingSummary.builder()
            .id(offering.getId())
            .section(offering.getSection())
            .course(CourseMapper.toSummary(offering.getCourse()))
            .instructor(InstructorMapper.toSummary(offering.getInstructor()))
            .semester(SemesterMapper.toSummary(offering.getSemester(), today))
            .build();
    }
}
