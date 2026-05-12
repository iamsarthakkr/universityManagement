package com.sarthak.universityEnrollmentManagement.mapper;


import com.sarthak.universityEnrollmentManagement.dto.internal.CreateInstructorCommand;
import com.sarthak.universityEnrollmentManagement.entity.InstructorEntity;

public class InstructorMapper {
    public static InstructorEntity toEntity(CreateInstructorCommand createInstructorCommand) {
        return InstructorEntity
            .builder()
            .firstName(createInstructorCommand.firstName())
            .lastName(createInstructorCommand.lastName())
            .phoneNumber(createInstructorCommand.phoneNumber())
            .department(createInstructorCommand.department())
            .build();
    }
}
