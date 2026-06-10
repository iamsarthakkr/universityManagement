package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.student.dto.CreateStudentCommand;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    
    @Transactional
    public StudentEntity getStudentByUserId(int userId) {
        return studentRepo
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No student exist for user id " + userId));
    }

}
