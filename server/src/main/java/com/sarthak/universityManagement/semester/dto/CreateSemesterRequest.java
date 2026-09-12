package com.sarthak.universityManagement.semester.dto;

import com.sarthak.universityManagement.semester.types.SemesterTerm;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateSemesterRequest(
    @NotNull(message = "term is required")
    SemesterTerm term,

    @NotNull(message = "year is required")
    Integer year,

    @NotNull(message = "registration start date required")
    LocalDate registrationStartDate,

    @NotNull(message = "registration end date required")
    LocalDate registrationEndDate,

    @NotNull(message = "start date required")
    LocalDate startDate,

    @NotNull(message = "end date required")
    LocalDate endDate
) {
}
