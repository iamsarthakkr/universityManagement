package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.student.dto.CreateStudentCommand;

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
            .department(createStudentCommand.department())
            .build();
    }
}
