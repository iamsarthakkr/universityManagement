package com.sarthak.universityManagement.semester.validators;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.types.SemesterStatus;

public class SemesterValidator {

    public static void validateSemesterDates(CreateSemesterRequest semesterRequest) {
        if(!semesterRequest.registrationStartDate().isBefore(semesterRequest.registrationEndDate())) {
            throw new BadRequestException("Registration start date cannot be after registration end date");
        }
        if(!semesterRequest.startDate().isBefore(semesterRequest.endDate())) {
            throw new BadRequestException("Start date cannot be before end date");
        }
    }

    public static void validateSemesterAllowsOfferings(SemesterEntity semester) {
        var status = semester.getStatus();
        if(status != SemesterStatus.PLANNED) {
            throw new BadRequestException("Semester doesn't allow offerings");
        }
    }

}
