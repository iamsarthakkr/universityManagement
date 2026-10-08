package com.sarthak.universityManagement.semester.types;

import java.util.Arrays;

public enum SemesterAction {
    PLAN(SemesterStatus.PLANNED),
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

    public static SemesterAction valueOf(SemesterStatus targetStatus) {
        return Arrays.stream(values())
            .filter(x -> x.targetStatus.equals(targetStatus))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot get semester action for status " + targetStatus));
    }
}
