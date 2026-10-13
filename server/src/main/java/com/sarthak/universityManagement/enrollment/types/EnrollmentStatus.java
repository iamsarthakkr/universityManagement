package com.sarthak.universityManagement.enrollment.types;

public enum EnrollmentStatus {
    PENDING,
    ENROLLED,
    REJECTED,
    CANCELLED,
    DROPPED;

    public boolean canTransitionTo(EnrollmentStatus target) {
        return switch (this) {
            case PENDING -> target == ENROLLED || target == REJECTED || target == CANCELLED;
            case ENROLLED -> target == DROPPED;
            case REJECTED,
                 CANCELLED,
                 DROPPED -> false;
        };
    }
}
