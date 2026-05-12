package com.sarthak.universityManagement.instructor;

import com.sarthak.universityManagement.instructor.dto.CreateInstructorCommand;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
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
            throw new BadRequestException("User must have INSTRUCTOR role");
        }
        
        InstructorEntity newInstructor = InstructorMapper.toEntity(createInstructorCommand);
        newInstructor.setUser(user);
        return instructorRepo.save(newInstructor);
    }
    
}
