package com.sarthak.universityManagement.registration.types;

public enum RegistrationAction {
    APPROVE(RegistrationStatus.APPROVED),
    REJECT(RegistrationStatus.REJECTED);

    private final RegistrationStatus targetStatus;
    RegistrationAction(RegistrationStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public RegistrationStatus getTargetStatus() {
        return targetStatus;
    }
}
