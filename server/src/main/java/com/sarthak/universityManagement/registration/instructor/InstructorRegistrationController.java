package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.common.rest.SuccessCode;
import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
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
@RequestMapping("instructor-registrations")
public class InstructorRegistrationController {
    private final InstructorRegistrationService instructorRegistrationService;

    @Autowired
    public InstructorRegistrationController(
        InstructorRegistrationService instructorRegistrationService
    ) {
        this.instructorRegistrationService = instructorRegistrationService;
    }

    @GetMapping
        public ResponseEntity<ApiResponse<List<InstructorRegistrationResponse>>> getInstructorRegistrations(
        RegistrationFilter filter
    ) {
        return Res.success(instructorRegistrationService.getRequests(filter));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InstructorRegistrationResponse>> createInstructorRegistration(
        @Valid @RequestBody InstructorRegistrationRequest instructorRegistrationRequest
    ) {
        var res = instructorRegistrationService.createRegistration(instructorRegistrationRequest);
        return Res.success(SuccessCode.CREATED, res);
    }

    @PostMapping("/{id}/{action}")
    public ResponseEntity<ApiResponse<InstructorRegistrationResponse>> performAction(
        @PathVariable Integer id,
        @PathVariable RegistrationAction action
    ) {
        return Res.success(instructorRegistrationService.performAction(id, action));
    }
}
