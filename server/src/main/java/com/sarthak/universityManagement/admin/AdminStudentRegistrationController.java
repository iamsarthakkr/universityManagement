package com.sarthak.universityManagement.admin;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.registration.student.StudentRegistrationService;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/student-registrations")
@RequiredArgsConstructor
public class AdminStudentRegistrationController {
    private final StudentRegistrationService studentRegistrationService;
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<StudentRegistrationResponse>>> getPendingRegistrations() {
        return Res.success(studentRegistrationService.getPendingRequests());
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<StudentRegistrationResponse>> approve(@PathVariable Integer id) {
        return Res.success(
            "Student registration approved successfully",
            studentRegistrationService.approveRegistration(id)
        );
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<StudentRegistrationResponse>> reject(@PathVariable Integer id) {
        return Res.success(
            "Student registration rejected",
            studentRegistrationService.rejectRegistration(id)
        );
    }
}
