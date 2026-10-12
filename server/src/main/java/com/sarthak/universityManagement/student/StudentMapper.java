package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.department.DepartmentMapper;
import com.sarthak.universityManagement.student.dto.CreateStudentCommand;
import com.sarthak.universityManagement.student.dto.StudentResponse;
import com.sarthak.universityManagement.student.dto.StudentSummary;

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
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .email(student.getUser().getEmail())
            .department(DepartmentMapper.toResponse(student.getDepartment()))
            .build();
    }

    public static StudentSummary toSummary(StudentEntity student) {
        return StudentSummary.builder()
            .id(student.getId())
            .firstName(student.getFirstName())
            .lastName(student.getLastName())
            .build();
    }
}
