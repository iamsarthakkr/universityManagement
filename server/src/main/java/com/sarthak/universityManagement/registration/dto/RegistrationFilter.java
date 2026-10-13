package com.sarthak.universityManagement.registration.dto;

import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import jakarta.annotation.Nullable;

public record RegistrationFilter(
    @Nullable Integer departmentId,
    @Nullable RegistrationStatus status
) { }
