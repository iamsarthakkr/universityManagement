package com.sarthak.universityManagement.enrollment.types;

import java.util.Arrays;

public enum EnrollmentAction {
    APPROVE(EnrollmentStatus.ENROLLED),
    REJECT(EnrollmentStatus.REJECTED),
    CANCEL(EnrollmentStatus.CANCELLED),
    DROP(EnrollmentStatus.DROPPED);

    private final EnrollmentStatus targetStatus;
    EnrollmentAction(EnrollmentStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public EnrollmentStatus getTargetStatus() {
        return targetStatus;
    }

    public static EnrollmentAction valueOf(EnrollmentStatus targetStatus) {
        return Arrays.stream(EnrollmentAction.values())
            .filter(a -> a.targetStatus.equals(targetStatus))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot get enrollment action for status " + targetStatus));
    }
}
