package com.sarthak.universityManagement.registration.types;

public enum RegistrationStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public boolean canTransitionTo(RegistrationStatus targetStatus) {
        return switch (this) {
            case PENDING -> targetStatus == APPROVED || targetStatus == REJECTED;
            case APPROVED,  REJECTED -> false;
        };
    }
}
