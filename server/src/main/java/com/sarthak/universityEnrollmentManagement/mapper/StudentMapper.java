package com.sarthak.universityEnrollmentManagement.mapper;

import com.sarthak.universityEnrollmentManagement.entity.StudentEntity;
import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;

public class StudentMapper {
    
    public static StudentEntity toEntityFromStudentRegistration(StudentRegistrationEntity entity) {
        return StudentEntity
            .builder()
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .phoneNumber(entity.getPhoneNumber())
            .dateOfBirth(entity.getDateOfBirth())
            .address(entity.getAddress())
            .fatherName(entity.getFatherName())
            .motherName(entity.getMotherName())
            .build();
    }
}
