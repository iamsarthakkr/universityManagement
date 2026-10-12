package com.sarthak.universityManagement.semester.types;

public enum SemesterStatus {
    PLANNED,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(SemesterStatus targetStatus) {
        return switch (this) {
            case PLANNED -> targetStatus == ACTIVE || targetStatus == CANCELLED;
            case ACTIVE -> targetStatus == COMPLETED || targetStatus == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
