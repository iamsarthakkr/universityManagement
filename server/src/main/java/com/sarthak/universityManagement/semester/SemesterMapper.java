package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.dto.SemesterResponse;
import com.sarthak.universityManagement.semester.dto.SemesterSummaryResponse;
import com.sarthak.universityManagement.semester.types.SemesterAction;

import java.time.LocalDate;
import java.util.List;

public final class SemesterMapper {
    public static SemesterEntity toEntity(CreateSemesterRequest semesterRequest) {
        return SemesterEntity.builder()
            .term(semesterRequest.term())
            .year(semesterRequest.year())
            .registrationStartDate(semesterRequest.registrationStartDate())
            .registrationEndDate(semesterRequest.registrationEndDate())
            .startDate(semesterRequest.startDate())
            .endDate(semesterRequest.endDate())
            .build();
    }

    public static SemesterResponse toResponse(SemesterEntity semesterEntity, LocalDate today, List<SemesterAction> allowedActions) {
        return SemesterResponse.builder()
            .id(semesterEntity.getId())
            .term(semesterEntity.getTerm())
            .year(semesterEntity.getYear())
            .status(semesterEntity.getStatus())
            .registrationStartDate(semesterEntity.getRegistrationStartDate())
            .registrationEndDate(semesterEntity.getRegistrationEndDate())
            .startDate(semesterEntity.getStartDate())
            .endDate(semesterEntity.getEndDate())
            .isRegistrationOpen(semesterEntity.isRegistrationOpen(today))
            .allowedActions(allowedActions)
            .build();
    }

    public static SemesterSummaryResponse toSummary(SemesterEntity semesterEntity, LocalDate today) {
        return SemesterSummaryResponse.builder()
            .id(semesterEntity.getId())
            .status(semesterEntity.getStatus())
            .term(semesterEntity.getTerm())
            .year(semesterEntity.getYear())
            .isRegistrationOpen(semesterEntity.isRegistrationOpen(today))
            .build();
    }
}
