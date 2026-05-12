package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.internal.CreateStudentCommand;
import com.sarthak.universityEnrollmentManagement.entity.StudentEntity;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.Role;
import com.sarthak.universityEnrollmentManagement.exceptions.BadRequestException;
import com.sarthak.universityEnrollmentManagement.exceptions.ConflictException;
import com.sarthak.universityEnrollmentManagement.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sarthak.universityEnrollmentManagement.repo.StudentRepo;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepo studentRepo;
    
    @Transactional
    public StudentEntity createStudentForUser(CreateStudentCommand createStudentCommand, UserEntity user) {
        if(studentRepo.existsByUserId(user.getId())) {
            throw new ConflictException("Student already exists for user with id " + user.getId());
        }
        if(user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User must have STUDENT role");
        }
        
        StudentEntity newStudent = StudentMapper.toEntity(createStudentCommand);
        newStudent.setUser(user);
        return studentRepo.save(newStudent);
    }

}
