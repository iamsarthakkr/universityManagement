package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.dto.SemesterResponse;

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

    public static SemesterResponse toResponse(SemesterEntity semesterEntity) {
        return SemesterResponse.builder()
            .term(semesterEntity.getTerm())
            .year(semesterEntity.getYear())
            .status(semesterEntity.getStatus())
            .registrationStartDate(semesterEntity.getRegistrationStartDate())
            .registrationEndDate(semesterEntity.getRegistrationEndDate())
            .startDate(semesterEntity.getStartDate())
            .endDate(semesterEntity.getEndDate())
            .build();
    }
}
