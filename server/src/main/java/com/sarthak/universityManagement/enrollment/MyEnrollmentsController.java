package com.sarthak.universityManagement.enrollment;


import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentFilter;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("me")
public class MyEnrollmentsController {
    private final EnrollmentService enrollmentService;

    @Autowired
    public MyEnrollmentsController(
        EnrollmentService enrollmentService
    ) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/enrollments")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
        EnrollmentFilter filter
    ) {
        return Res.success(enrollmentService.getEnrollmentsForStudent(filter));
    }

}
