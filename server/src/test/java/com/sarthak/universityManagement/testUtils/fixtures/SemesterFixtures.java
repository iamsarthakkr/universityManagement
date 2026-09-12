package com.sarthak.universityManagement.testUtils.fixtures;

import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;

import java.time.LocalDate;
import java.time.Year;
import java.util.Date;

public final class SemesterFixtures {
    public static SemesterEntity.SemesterEntityBuilder semester() {
        return SemesterEntity.builder()
            .term(SemesterTerm.SUMMER)
            .year(2026)
            .status(SemesterStatus.PLANNED)
            .registrationStartDate(LocalDate.of(2026, 1, 1))
            .registrationEndDate(LocalDate.of(2026, 1, 30))
            .startDate(LocalDate.of(2026, 1, 25))
            .endDate(LocalDate.of(2026, 2, 28));
    }

    public static CreateSemesterRequest.CreateSemesterRequestBuilder semesterRequest() {
        return CreateSemesterRequest.builder()
            .term(SemesterTerm.SUMMER)
            .year(2026)
            .registrationStartDate(LocalDate.of(2026, 1, 1))
            .registrationEndDate(LocalDate.of(2026, 1, 30))
            .startDate(LocalDate.of(2026, 1, 25))
            .endDate(LocalDate.of(2026, 2, 28));
    }
}
