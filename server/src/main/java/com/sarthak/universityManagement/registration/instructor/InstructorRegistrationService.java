package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.user.UserService;
import com.sarthak.universityManagement.registration.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorRegistrationService {
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final RegistrationValidator registrationValidator;
    private final UserService userService;
    private final InstructorService instructorService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public void createRegistration(InstructorRegistrationRequest instructorRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(instructorRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(instructorRegistrationRequest.email());
        
        InstructorRegistrationEntity toSave = InstructorRegistrationMapper.toEntity(instructorRegistrationRequest);
        toSave.setPassword(passwordEncoder.encode(instructorRegistrationRequest.password()));
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        
        instructorRegistrationRepo.save(toSave);
    }
    
    @Transactional(readOnly = true)
    public List<InstructorRegistrationResponse> getPendingRequests() {
        return instructorRegistrationRepo
            .findByRegistrationStatus(RegistrationStatus.PENDING)
            .stream()
            .map(InstructorRegistrationMapper::toResponse)
            .toList();
    }
    
    @Transactional
    public void approveRegistration(int registrationId) {
        InstructorRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        
        // create user
        UserEntity user = userService.createUserForRegistration(InstructorRegistrationMapper.toCreateUserCommand(registration));
        
        // create associated instructor
        instructorService.createInstructorForUser(InstructorRegistrationMapper.toCreateInstructorCommand(registration), user);
        
        // approve registration
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(null); // TODO: need to set once security is done
    }
    
    @Transactional
    public void rejectRegistration(int registrationId) {
        InstructorRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        registration.setRegistrationStatus(RegistrationStatus.REJECTED);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(null); // TODO: need to set once security is done
    }
    
    private InstructorRegistrationEntity getPendingRegistrationOrThrow(int registrationId) {
        InstructorRegistrationEntity registration = instructorRegistrationRepo
            .findById(registrationId)
            .orElseThrow(() -> new ResourceNotFoundException("Instructor registration not found with id " + registrationId));
        
        RegistrationStatus currentStatus = registration.getRegistrationStatus();
        if(registration.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new ConflictException("Registration already finalized with status " + currentStatus + " for given registration id " + registrationId);
        }
        return registration;
    }
    
}
