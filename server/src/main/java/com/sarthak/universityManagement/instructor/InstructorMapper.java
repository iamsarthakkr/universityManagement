package com.sarthak.universityManagement.instructor;


import com.sarthak.universityManagement.department.DepartmentMapper;
import com.sarthak.universityManagement.instructor.dto.CreateInstructorCommand;
import com.sarthak.universityManagement.instructor.dto.InstructorResponse;
import com.sarthak.universityManagement.instructor.dto.InstructorSummary;

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

    public static InstructorResponse toResponse(InstructorEntity instructorEntity) {
        return InstructorResponse.builder()
            .id(instructorEntity.getId())
            .firstName(instructorEntity.getFirstName())
            .lastName(instructorEntity.getLastName())
            .email(instructorEntity.getUser().getEmail())
            .department(DepartmentMapper.toResponse(instructorEntity.getDepartment()))
            .build();
    }

    public static InstructorSummary toSummary(InstructorEntity instructorEntity) {
        return InstructorSummary.builder()
            .id(instructorEntity.getId())
            .firstName(instructorEntity.getFirstName())
            .lastName(instructorEntity.getLastName())
            .build();
    }

}
