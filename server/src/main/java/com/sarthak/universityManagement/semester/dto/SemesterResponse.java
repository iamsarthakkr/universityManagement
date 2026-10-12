package com.sarthak.universityManagement.semester.dto;

import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SemesterResponse(
    Integer id,
    SemesterTerm term,
    Integer year,
    SemesterStatus status,
    LocalDate registrationStartDate,
    LocalDate registrationEndDate,
    LocalDate startDate,
    LocalDate endDate,
    List<SemesterAction> allowedActions,
    boolean isRegistrationOpen,
    boolean acceptsOfferings
) {}
