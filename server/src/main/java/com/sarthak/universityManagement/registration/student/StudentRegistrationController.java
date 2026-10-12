package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.common.rest.SuccessCode;
import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("student-registrations")
public class StudentRegistrationController {
    private final StudentRegistrationService studentRegistrationService;

    @Autowired
    public StudentRegistrationController(
        StudentRegistrationService studentRegistrationService
    ) {
        this.studentRegistrationService = studentRegistrationService;
    }

    @GetMapping
        public ResponseEntity<ApiResponse<List<StudentRegistrationResponse>>> getStudentRegistrations(
        RegistrationFilter filter
    ) {
        return Res.success(studentRegistrationService.getRequests(filter));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentRegistrationResponse>> createStudentRegistration(
        @Valid @RequestBody StudentRegistrationRequest studentRegistrationRequest
    ) {
        var res = studentRegistrationService.createRegistration(studentRegistrationRequest);
        return Res.success(SuccessCode.CREATED, res);
    }

    @PostMapping("/{id}/{action}")
    public ResponseEntity<ApiResponse<StudentRegistrationResponse>> performAction(
        @PathVariable Integer id,
        @PathVariable RegistrationAction action
    ) {
        return Res.success(studentRegistrationService.performAction(id, action));
    }
}
