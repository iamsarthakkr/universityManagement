package com.sarthak.universityManagement.instructor;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.instructor.dto.CreateInstructorCommand;
import com.sarthak.universityManagement.instructor.dto.InstructorFilter;
import com.sarthak.universityManagement.instructor.dto.InstructorResponse;
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
public class InstructorService {
    private final InstructorRepo instructorRepo;

    @Autowired
    public InstructorService(InstructorRepo instructorRepo) {
        this.instructorRepo = instructorRepo;
    }

    @Transactional
    public InstructorEntity createInstructorForUser(CreateInstructorCommand createInstructorCommand, UserEntity user) {
        if(instructorRepo.existsByUserId(user.getId())) {
            throw new ConflictException("Instructor already exists for user with id " + user.getId());
        }
        if(user.getRole() != Role.INSTRUCTOR) {
            throw new BadRequestException("User must have INSTRUCTOR role");
        }

        InstructorEntity newInstructor = InstructorMapper.toEntity(createInstructorCommand);
        newInstructor.setUser(user);
        return instructorRepo.save(newInstructor);
    }

    @Transactional(readOnly = true)
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public List<InstructorResponse> getInstructors(InstructorFilter filter) {
        return instructorRepo
            .search(filter)
            .stream()
            .map(InstructorMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public InstructorEntity getInstructorEntity(Integer instructorId) {
        return instructorRepo
            .findById(instructorId)
            .orElseThrow(() -> new ResourceNotFoundException("Instructor does not exist with id " + instructorId));
    }

    @Transactional(readOnly = true)
    public InstructorEntity getInstructorByUserId(Integer userId) {
        return instructorRepo
            .findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("No instructor exist for user id " + userId));
    }
}
