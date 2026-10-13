package com.sarthak.universityManagement.student;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.student.dto.CreateStudentCommand;
import com.sarthak.universityManagement.student.dto.StudentFilter;
import com.sarthak.universityManagement.student.dto.StudentResponse;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepo studentRepo;

    @Autowired
    public StudentService(StudentRepo studentRepo) {
        this.studentRepo = studentRepo;
    }

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

    @Transactional(readOnly = true)
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public List<StudentResponse> getStudents(StudentFilter filter) {
        return studentRepo
            .search(filter)
            .stream()
            .map(StudentMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public StudentEntity getStudentByUserId(int userId) {
        return studentRepo
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No student exist for user id " + userId));
    }

    @Transactional(readOnly = true)
    public StudentEntity getStudentEntity(Integer studentId) {
        return studentRepo
            .findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("No student exists for student id " + studentId));
    }

}
