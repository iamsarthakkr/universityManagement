package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.student.dto.CreateStudentCommand;
import com.sarthak.universityManagement.student.dto.StudentResponse;

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

    public static StudentResponse toResponse(StudentEntity student) {
        return StudentResponse.builder()
            .id(student.getId())
            .name(student.getFirstName() + (student.getLastName() == null ? "" : " " + student.getLastName()))
            .build();
    }
}
