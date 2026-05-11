package com.sarthak.universityEnrollmentManagement.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StudentResponse {
    int id;
    String firstName;
    String lastName;
    String email;
}
