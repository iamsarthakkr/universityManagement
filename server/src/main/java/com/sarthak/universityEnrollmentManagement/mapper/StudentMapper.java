package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.dto.internal.CreateStudentCommand;
import com.sarthak.universityEnrollmentManagement.entity.StudentEntity;

public class StudentMapper {
    
    public static StudentEntity toEntity(CreateStudentCommand createStudentCommand) {
        return StudentEntity
            .builder()
            .firstName(createStudentCommand.firstName())
            .lastName(createStudentCommand.lastName())
            .phoneNumber(createStudentCommand.phoneNumber())
            .dateOfBirth(createStudentCommand.dateOfBirth())
            .address(createStudentCommand.address())
            .fatherName(createStudentCommand.fatherName())
            .motherName(createStudentCommand.motherName())
            .build();
    }
}
