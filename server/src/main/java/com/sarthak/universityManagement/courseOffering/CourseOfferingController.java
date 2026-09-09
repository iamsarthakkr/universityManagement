package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.common.rest.SuccessCode;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("course-offerings")
public class CourseOfferingController {
    private final CourseOfferingService courseOfferingService;

    @Autowired
    public CourseOfferingController(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> create(@Valid @RequestBody CreateCourseOfferingRequest courseOfferingRequest) {
        return Res.success(SuccessCode.CREATED, courseOfferingService.createOffering(courseOfferingRequest));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseOfferingResponse>>> getOfferings(@RequestParam("semesterId") Integer semesterId) {
        return Res.success(courseOfferingService.getOfferingsBySemester(semesterId));
    }

    @GetMapping("/{offeringId}")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> getOffering(@PathVariable Integer offeringId) {
        return Res.success(courseOfferingService.getOffering(offeringId));
    }

}
