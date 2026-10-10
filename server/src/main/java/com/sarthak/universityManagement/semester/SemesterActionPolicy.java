package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.semester.types.SemesterAction;

import java.util.Arrays;
import java.util.List;

public class SemesterActionPolicy {
    public static List<SemesterAction> allowedActions(
        SemesterEntity semester,
        Role actorRole
    ) {
        return Arrays.stream(SemesterAction.values())
            .filter(action -> canPerform(semester, action, actorRole))
            .toList();
    }

    public static boolean canPerform(
        SemesterEntity semester,
        SemesterAction action,
        Role actorRole
    ) {
        return Role.ADMIN.equals(actorRole)
            && semester.canTransitionTo(action.getTargetStatus());
    }

}
