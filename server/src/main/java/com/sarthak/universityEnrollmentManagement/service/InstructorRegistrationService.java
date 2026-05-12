package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.request.InstructorRegistrationRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.InstructorRegistrationResponse;
import com.sarthak.universityEnrollmentManagement.entity.InstructorRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import com.sarthak.universityEnrollmentManagement.exceptions.ConflictException;
import com.sarthak.universityEnrollmentManagement.exceptions.ResourceNotFoundException;
import com.sarthak.universityEnrollmentManagement.mapper.InstructorRegistrationMapper;
import com.sarthak.universityEnrollmentManagement.repo.InstructorRegistrationRepo;
import com.sarthak.universityEnrollmentManagement.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
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
    
    @Transactional
    public void createRegistration(InstructorRegistrationRequest instructorRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(instructorRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(instructorRegistrationRequest.email());
        
        InstructorRegistrationEntity toSave = InstructorRegistrationMapper.toEntity(instructorRegistrationRequest);
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
