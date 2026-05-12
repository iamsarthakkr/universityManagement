package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.internal.CreateInstructorCommand;
import com.sarthak.universityEnrollmentManagement.entity.InstructorEntity;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.Role;
import com.sarthak.universityEnrollmentManagement.exceptions.BadRequestException;
import com.sarthak.universityEnrollmentManagement.exceptions.ConflictException;
import com.sarthak.universityEnrollmentManagement.mapper.InstructorMapper;
import com.sarthak.universityEnrollmentManagement.repo.InstructorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final InstructorRepo instructorRepo;
    
    @Transactional
    public InstructorEntity createInstructorForUser(CreateInstructorCommand createInstructorCommand, UserEntity user) {
        if(instructorRepo.existsByUserId(user.getId())) {
            throw new ConflictException("Instructor already exists for user with id " + user.getId());
        }
        if(user.getRole() != Role.INSTRUCTOR) {
            throw new BadRequestException("User must have STUDENT role");
        }
        
        InstructorEntity newInstructor = InstructorMapper.toEntity(createInstructorCommand);
        newInstructor.setUser(user);
        return instructorRepo.save(newInstructor);
    }
    
}
