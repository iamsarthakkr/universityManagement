package com.sarthak.universityManagement.student.dto;

import lombok.Builder;

@Builder
public record StudentSummary(
    Integer id,
    String firstName,
    String lastName
) { }
