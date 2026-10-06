package com.sarthak.universityManagement.semester.dto;

import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import lombok.Builder;

@Builder
public record SemesterSummaryResponse(
    Integer id,
    SemesterTerm term,
    Integer year,
    SemesterStatus status,
    boolean isRegistrationOpen
) {}
