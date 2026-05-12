package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.request.StudentRegistrationRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.StudentRegistrationResponse;
import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.UserEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import com.sarthak.universityEnrollmentManagement.exceptions.ConflictException;
import com.sarthak.universityEnrollmentManagement.exceptions.ResourceNotFoundException;
import com.sarthak.universityEnrollmentManagement.mapper.StudentRegistrationMapper;
import com.sarthak.universityEnrollmentManagement.repo.StudentRegistrationRepo;
import com.sarthak.universityEnrollmentManagement.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentRegistrationService {
    private final RegistrationValidator registrationValidator;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final UserService userService;
    private final StudentService studentService;
    
    @Transactional
    public void createRegistration(StudentRegistrationRequest studentRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(studentRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(studentRegistrationRequest.email());
        
        StudentRegistrationEntity toSave = StudentRegistrationMapper.toEntity(studentRegistrationRequest);
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        studentRegistrationRepo.save(toSave);
    }
    
    @Transactional(readOnly = true)
    public List<StudentRegistrationResponse> getPendingRequests() {
        return studentRegistrationRepo
            .findByRegistrationStatus(RegistrationStatus.PENDING)
            .stream()
            .map(StudentRegistrationMapper::toResponse)
            .toList();
    }
    
    @Transactional
    public void approveRegistration(int registrationId) {
        StudentRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        
        // create user
        UserEntity user = userService.createUserForRegistration(registration);
        
        // create associated student
        studentService.createStudentForUser(user, registration);
        
        // approve registration
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(null); // TODO: need to set once security is done
    }
    
    @Transactional
    public void rejectRegistration(int registrationId) {
        StudentRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        registration.setRegistrationStatus(RegistrationStatus.REJECTED);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(null); // TODO: need to set once security is done
    }
    
    private StudentRegistrationEntity getPendingRegistrationOrThrow(int registrationId) {
        StudentRegistrationEntity registration = studentRegistrationRepo
            .findById(registrationId)
            .orElseThrow(() -> new ResourceNotFoundException("Student registration not found with id " + registrationId));
        
        RegistrationStatus currentStatus = registration.getRegistrationStatus();
        if(registration.getRegistrationStatus() != RegistrationStatus.PENDING) {
            throw new ConflictException("Registration already finalized with status " + currentStatus + " for given registration id " + registrationId);
        }
        return registration;
    }
    
}
