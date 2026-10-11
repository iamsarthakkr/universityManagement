package com.sarthak.universityManagement.courseOffering.types;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ForbiddenException;

public enum EnrollDenial {
    NOT_A_STUDENT,
    OFFERING_FULL,
    REGISTRATION_CLOSED;

    public RuntimeException toException(Integer offeringId) {
        return switch (this) {
            case NOT_A_STUDENT -> new ForbiddenException("You are not permitted to enroll on the offering with id " + offeringId);
            case OFFERING_FULL -> new ConflictException("Course offering is full");
            case REGISTRATION_CLOSED -> new BadRequestException("Semester no longer allows enrollments");
        };
    }
}