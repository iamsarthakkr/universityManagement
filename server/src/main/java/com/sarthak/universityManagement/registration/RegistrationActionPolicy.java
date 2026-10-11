package com.sarthak.universityManagement.registration;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActionPolicy {
    public static List<RegistrationAction> allowedActions(RegistrationStatus from, Role actorRole) {
        if(actorRole != Role.ADMIN) return List.of();

        List<RegistrationAction> allowedActions = new ArrayList<>();
        for(RegistrationAction action: RegistrationAction.values()) {
            if(from.canTransitionTo(action.getTargetStatus())) allowedActions.add(action);
        }

        return allowedActions;
    }

}
