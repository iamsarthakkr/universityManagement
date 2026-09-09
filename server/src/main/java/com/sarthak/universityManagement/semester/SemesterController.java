package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.common.rest.SuccessCode;
import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.dto.SemesterResponse;
import com.sarthak.universityManagement.semester.dto.SemesterTransitionRequest;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("semesters")
public class SemesterController {

    private final SemesterService semesterService;

    @Autowired
    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> getSemesters() {
        return Res.success(semesterService.getSemesters());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SemesterResponse>> create(@Valid @RequestBody CreateSemesterRequest createSemesterRequest) {
        return Res.success(SuccessCode.CREATED,  semesterService.createSemester(createSemesterRequest));
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemester(@PathVariable Integer semesterId) {
        return Res.success(semesterService.getSemester(semesterId));
    }

    @PatchMapping("/{semesterId}/status")
    public ResponseEntity<ApiResponse<Void>> pathStatus(
        @PathVariable Integer semesterId,
        @Valid @RequestBody SemesterTransitionRequest semesterTransitionRequest
    ) {
        semesterService.transition(semesterId, semesterTransitionRequest.status());
        return Res.success();
    }

}
