package com.sarthak.universityManagement.semester.types;

public enum SemesterAction {
    ACTIVATE(SemesterStatus.ACTIVE),
    COMPLETE(SemesterStatus.COMPLETED),
    CANCEL(SemesterStatus.CANCELLED);

    private final SemesterStatus targetStatus;

    SemesterAction(SemesterStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public SemesterStatus getTargetStatus() {
        return targetStatus;
    }
}
