package com.sarthak.universityManagement.semester.validators;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.types.SemesterStatus;

public class SemesterValidator {

    public static void validateSemesterAllowsOfferings(SemesterEntity semester) {
        var status = semester.getStatus();
        if(status != SemesterStatus.PLANNED) {
            throw new BadRequestException("Semester doesn't allow offerings");
        }
    }

}
