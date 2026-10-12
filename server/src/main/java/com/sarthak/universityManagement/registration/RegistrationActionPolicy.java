package com.sarthak.universityManagement.registration;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;

import java.util.Arrays;
import java.util.List;

public class RegistrationActionPolicy {
    public static List<RegistrationAction> allowedActions(RegistrationStatus from, Role actorRole) {
        return Arrays.stream(RegistrationAction.values())
            .filter(action -> canPerform(action, from, actorRole))
            .toList();
    }

    public static boolean canPerform(RegistrationAction action, RegistrationStatus from, Role actorRole) {
        return actorRole == Role.ADMIN &&
            from.canTransitionTo(action.getTargetStatus());
    }

}
