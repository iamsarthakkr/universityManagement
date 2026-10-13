package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingFilter;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("me")
public class MyCourseOfferingsController {
    private final CourseOfferingService courseOfferingService;

    @Autowired
    public MyCourseOfferingsController(
        CourseOfferingService courseOfferingService
    ) {
        this.courseOfferingService = courseOfferingService;
    }

    @GetMapping("/course-offerings")
    public ResponseEntity<ApiResponse<List<CourseOfferingResponse>>> getMyCourseOfferings(
        CourseOfferingFilter filter
    ) {
        return Res.success(courseOfferingService.getCourseOfferingsForInstructor(filter));
    }
}
