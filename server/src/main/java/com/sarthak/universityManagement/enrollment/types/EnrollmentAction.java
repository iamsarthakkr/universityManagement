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

}
