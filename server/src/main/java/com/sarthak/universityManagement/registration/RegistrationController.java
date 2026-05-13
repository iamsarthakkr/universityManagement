package com.sarthak.universityManagement.registration;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.common.rest.SuccessCode;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationService;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.registration.student.StudentRegistrationService;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("registration")
@RequiredArgsConstructor
public class RegistrationController {
    private final StudentRegistrationService studentRegistrationService;
    private final InstructorRegistrationService instructorRegistrationService;
    
    @PostMapping("/student")
    public ResponseEntity<ApiResponse<StudentRegistrationResponse>> createStudentRegistration(
        @RequestBody StudentRegistrationRequest studentRegistrationRequest
    ) {
        StudentRegistrationResponse res = studentRegistrationService.createRegistration(studentRegistrationRequest);
        return Res.success(SuccessCode.CREATED, null,  res);
    }
    
    @PostMapping("/instructor")
    public ResponseEntity<ApiResponse<InstructorRegistrationResponse>> createInstructorRegistration(
        @RequestBody InstructorRegistrationRequest instructorRegistrationRequest
    ) {
        InstructorRegistrationResponse res = instructorRegistrationService.createRegistration(instructorRegistrationRequest);
        return Res.success(SuccessCode.CREATED, null,  res);
    }
}
