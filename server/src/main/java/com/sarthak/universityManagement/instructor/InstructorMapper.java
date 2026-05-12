package com.sarthak.universityManagement.instructor;


import com.sarthak.universityManagement.instructor.dto.CreateInstructorCommand;

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
