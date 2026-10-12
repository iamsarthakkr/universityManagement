package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    public EnrollmentController(
        EnrollmentService enrollmentService
    ) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollment(
        @PathVariable Integer enrollmentId
    ) {
        return Res.success(enrollmentService.getEnrollment(enrollmentId));
    }

    @PostMapping("/{enrollmentId}/{action}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> performAction(
        @PathVariable Integer enrollmentId,
        @PathVariable EnrollmentAction action
    ) {
        return Res.success(enrollmentService.performAction(enrollmentId, action));
    }

}
