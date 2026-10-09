package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollment(
        @PathVariable Integer enrollmentId
    ) {
        return Res.success(enrollmentService.getEnrollment(enrollmentId));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStudentEnrollments(
        @RequestParam(required = false) Integer semesterId,
        @RequestParam(required = false)EnrollmentStatus enrollmentStatus
    ) {
        var studentId = currentUserService.getCurrentStudent().getId();

        return Res.success(enrollmentService.getEnrollmentsForStudent(studentId,  semesterId, enrollmentStatus));
    }

    @PostMapping("/{enrollmentId}/{action}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> performAction(
        @PathVariable Integer enrollmentId,
        @PathVariable EnrollmentAction action
    ) {
        return Res.success(enrollmentService.performAction(enrollmentId, action));

    }

}
