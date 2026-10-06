package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemesterActionPolicy {
    public static final Map<SemesterStatus, List<SemesterStatus>> AllowedSemesterTransitions;

    static {
        AllowedSemesterTransitions = new HashMap<>();

        AllowedSemesterTransitions.putIfAbsent(SemesterStatus.PLANNED,
            List.of(SemesterStatus.ACTIVE, SemesterStatus.CANCELLED));

        AllowedSemesterTransitions.putIfAbsent(SemesterStatus.ACTIVE,
            List.of(SemesterStatus.COMPLETED, SemesterStatus.CANCELLED));

        AllowedSemesterTransitions.putIfAbsent(SemesterStatus.COMPLETED, List.of());
        AllowedSemesterTransitions.putIfAbsent(SemesterStatus.CANCELLED, List.of());
    }

    public static List<SemesterAction> allowedActions(
        SemesterEntity semester,
        Role actorRole
    ) {
        return Arrays.stream(SemesterAction.values())
            .filter(action -> canPerform(semester, actorRole, action))
            .toList();
    }

    public static boolean canPerform(
        SemesterEntity semester,
        Role actorRole,
        SemesterAction action
    ) {
        return Role.ADMIN.equals(actorRole)
            && semester.canTransitionTo(action.getTargetStatus());
    }

}
