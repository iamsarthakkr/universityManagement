package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.department.DepartmentService;
import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.registration.RegistrationActionPolicy;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.user.CurrentUserService;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.user.UserService;
import com.sarthak.universityManagement.registration.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorRegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final RegistrationValidator registrationValidator;
    private final UserService userService;
    private final InstructorService instructorService;
    private final CurrentUserService currentUserService;
    private final DepartmentService departmentService;
    private final Clock clock;
    
    @Transactional
    public InstructorRegistrationResponse createRegistration(InstructorRegistrationRequest instructorRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(instructorRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(instructorRegistrationRequest.email());
        registrationValidator.validateDepartmentAvailable(instructorRegistrationRequest.departmentId());

        DepartmentEntity department = departmentService.getDepartmentById(instructorRegistrationRequest.departmentId());
        InstructorRegistrationEntity toSave = InstructorRegistrationMapper.toEntity(instructorRegistrationRequest);
        toSave.setPassword(passwordEncoder.encode(instructorRegistrationRequest.password()));
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        toSave.setDepartment(department);

        return InstructorRegistrationMapper.toResponse(instructorRegistrationRepo.save(toSave), List.of());
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public List<InstructorRegistrationResponse> getRequests(RegistrationStatus status) {
        return instructorRegistrationRepo
            .findByRegistrationStatus(status)
            .stream()
            .map(this::toResponse)
            .toList();
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public InstructorRegistrationResponse approveRegistration(int registrationId) {
        InstructorRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        
        // create user
        UserEntity user = userService.createUserForRegistration(InstructorRegistrationMapper.toCreateUserCommand(registration));
        
        // create associated instructor
        instructorService.createInstructorForUser(InstructorRegistrationMapper.toCreateInstructorCommand(registration), user);
        
        // approve registration
        updateRegistration(registration, RegistrationStatus.APPROVED);

        return toResponse(registration);
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public InstructorRegistrationResponse rejectRegistration(int registrationId) {
        InstructorRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        updateRegistration(registration, RegistrationStatus.REJECTED);
        return toResponse(registration);
    }

    /* ------------------------------------------------------ helpers ------------------------------------------------ */

    private InstructorRegistrationResponse toResponse(InstructorRegistrationEntity registration) {
        var actions = RegistrationActionPolicy.allowedActions(registration.getRegistrationStatus(), currentUserService.getCurrentUserRole());
        return InstructorRegistrationMapper.toResponse(registration, actions);
    }

    private void updateRegistration(InstructorRegistrationEntity registration, RegistrationStatus status) {
        registration.setRegistrationStatus(status);
        registration.setReviewedAt(Instant.now(clock));
        registration.setReviewedBy(currentUserService.getCurrentUser());
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
