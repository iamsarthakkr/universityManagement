package com.sarthak.universityManagement.admin;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationService;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/instructor-registrations")
@RequiredArgsConstructor
public class AdminInstructorRegistrationController {
    private final InstructorRegistrationService instructorRegistrationService;
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<InstructorRegistrationResponse>>> getPendingRegistrations() {
        return Res.success(instructorRegistrationService.getPendingRequests());
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<InstructorRegistrationResponse>> approve(@PathVariable Integer id) {
        return Res.success(
            "Student registration approved successfully",
            instructorRegistrationService.approveRegistration(id)
        );
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<InstructorRegistrationResponse>> reject(@PathVariable Integer id) {
        return Res.success(
            "Student registration rejected",
            instructorRegistrationService.rejectRegistration(id)
        );
    }
}
