package com.sarthak.universityManagement.semester.validators;

import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import lombok.NonNull;

public final class SemesterValidator {

    public boolean validateStatusTransition(
        @NonNull SemesterEntity semester,
        @NonNull SemesterStatus newStatus
    ) {
        return switch (newStatus) {
            case PLANNED -> false;
            case ACTIVE -> semester.getStatus() == SemesterStatus.PLANNED;
            case COMPLETED -> semester.getStatus() == SemesterStatus.ACTIVE;
            case CANCELLED -> semester.getStatus() == SemesterStatus.ACTIVE ||
                semester.getStatus() == SemesterStatus.PLANNED;
        };
    }

}
