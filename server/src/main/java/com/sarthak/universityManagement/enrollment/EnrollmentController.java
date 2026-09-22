package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentDetailResponse;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final CurrentUserService currentUserService;

    public EnrollmentController(
        EnrollmentService enrollmentService,
        CurrentUserService currentUserService
    ) {
        this.enrollmentService = enrollmentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<EnrollmentDetailResponse>>> getStudentEnrollments() {
        var studentId = currentUserService.getCurrentStudent().getId();

        return Res.success(enrollmentService.getEnrollmentsForStudent(studentId));
    }

    @PostMapping("/{enrollmentId}/approve")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> approveEnrollment(@PathVariable("enrollmentId") Integer enrollmentId) {
        return Res.success(enrollmentService.approveEnrollment(enrollmentId));
    }

    @PostMapping("/{enrollmentId}/reject")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> rejectEnrollment(@PathVariable("enrollmentId") Integer enrollmentId) {
        return Res.success(enrollmentService.rejectEnrollment(enrollmentId));
    }

    @PostMapping("/{enrollmentId}/drop")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> dropEnrollment(@PathVariable("enrollmentId") Integer enrollmentId) {
        return Res.success(enrollmentService.dropEnrollment(enrollmentId));
    }

    @PostMapping("/{enrollmentId}/cancel")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> cancelEnrollment(@PathVariable("enrollmentId") Integer enrollmentId) {
        return Res.success(enrollmentService.cancelEnrollment(enrollmentId));
    }
}
