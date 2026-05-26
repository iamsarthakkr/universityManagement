package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.student.StudentService;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.user.UserService;
import com.sarthak.universityManagement.registration.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public StudentRegistrationResponse createRegistration(StudentRegistrationRequest studentRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(studentRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(studentRegistrationRequest.email());
        
        StudentRegistrationEntity toSave = StudentRegistrationMapper.toEntity(studentRegistrationRequest);
        toSave.setPassword(passwordEncoder.encode(studentRegistrationRequest.password()));
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        
        StudentRegistrationEntity saved = studentRegistrationRepo.save(toSave);
        return StudentRegistrationMapper.toResponse(saved);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public List<StudentRegistrationResponse> getPendingRequests() {
        return studentRegistrationRepo
            .findByRegistrationStatus(RegistrationStatus.PENDING)
            .stream()
            .map(StudentRegistrationMapper::toResponse)
            .toList();
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public StudentRegistrationResponse approveRegistration(int registrationId) {
        StudentRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        
        // create user
        UserEntity user = userService.createUserForRegistration(StudentRegistrationMapper.toCreateUserCommand(registration));
        
        // create associated student
        studentService.createStudentForUser(StudentRegistrationMapper.toCreateStudentCommand(registration), user);
        
        // approve registration
        registration.setRegistrationStatus(RegistrationStatus.APPROVED);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(null); // TODO: need to set once security is done
        
        return StudentRegistrationMapper.toResponse(registration);
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public StudentRegistrationResponse rejectRegistration(int registrationId) {
        StudentRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        registration.setRegistrationStatus(RegistrationStatus.REJECTED);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(null); // TODO: need to set once security is done
        
        return StudentRegistrationMapper.toResponse(registration);
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
