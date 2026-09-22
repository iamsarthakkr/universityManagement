package com.sarthak.universityManagement.student.dto;

import lombok.Builder;

@Builder
public record StudentResponse(
    Integer id,
    String name
) {
}
