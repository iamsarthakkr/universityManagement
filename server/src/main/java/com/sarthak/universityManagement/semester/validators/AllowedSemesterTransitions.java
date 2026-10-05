package com.sarthak.universityManagement.semester.validators;

import com.sarthak.universityManagement.semester.types.SemesterStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllowedSemesterTransitions {
    public static final Map<SemesterStatus, List<SemesterStatus>> allowedTransitions;

    static {
        allowedTransitions = new HashMap<>();

        allowedTransitions.putIfAbsent(SemesterStatus.PLANNED,
            List.of(SemesterStatus.ACTIVE, SemesterStatus.CANCELLED));

        allowedTransitions.putIfAbsent(SemesterStatus.ACTIVE,
            List.of(SemesterStatus.COMPLETED, SemesterStatus.CANCELLED));

        allowedTransitions.putIfAbsent(SemesterStatus.COMPLETED, List.of());
        allowedTransitions.putIfAbsent(SemesterStatus.CANCELLED, List.of());
    }
}
