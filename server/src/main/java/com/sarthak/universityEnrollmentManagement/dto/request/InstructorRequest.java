package com.sarthak.universityEnrollmentManagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class InstructorRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String department;
}
