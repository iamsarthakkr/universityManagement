package com.sarthak.universityManagement.enrollment.types;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ForbiddenException;

public enum EnrollmentDenial {
    NOT_PERMITTED,
    INVALID_TRANSITION,
    SEMESTER_CLOSED,
    OFFERING_FULL;

    public RuntimeException toException(Integer enrollmentId) {
        return switch (this) {
            case NOT_PERMITTED -> new ForbiddenException("You are not permitted to perform this action on the enrollment with id " + enrollmentId);
            case INVALID_TRANSITION -> new BadRequestException("Enrollment with id " + enrollmentId + " cannot transition from its current status");
            case SEMESTER_CLOSED -> new BadRequestException("Semester no longer allows enrollment changes");
            case OFFERING_FULL -> new ConflictException("Course offering is full");
        };
    }

}
