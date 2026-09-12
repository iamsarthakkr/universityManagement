package com.sarthak.universityManagement.semester.dto;

import com.sarthak.universityManagement.semester.types.SemesterStatus;
import jakarta.validation.constraints.NotNull;

public record SemesterTransitionRequest(
    @NotNull SemesterStatus status
) {}
