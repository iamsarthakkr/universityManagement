package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.department.DepartmentService;
import com.sarthak.universityManagement.registration.RegistrationActionPolicy;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.student.StudentService;
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
public class StudentRegistrationService {
    private final RegistrationValidator registrationValidator;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final DepartmentService departmentService;
    private final Clock clock;

    @Transactional
    public StudentRegistrationResponse createRegistration(StudentRegistrationRequest studentRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(studentRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(studentRegistrationRequest.email());

        DepartmentEntity department = departmentService.getDepartmentById(studentRegistrationRequest.departmentId());
        StudentRegistrationEntity toSave = StudentRegistrationMapper.toEntity(studentRegistrationRequest);
        toSave.setPassword(passwordEncoder.encode(studentRegistrationRequest.password()));
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        toSave.setDepartment(department);

        return StudentRegistrationMapper.toResponse(studentRegistrationRepo.save(toSave), List.of());
    }
    
    
    @Transactional(readOnly = true)
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public List<StudentRegistrationResponse> getRequests(RegistrationStatus status) {
        return studentRegistrationRepo
            .findByRegistrationStatus(status)
            .stream()
            .map(this::toResponse)
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
        updateRegistration(registration, RegistrationStatus.APPROVED);
        return toResponse(registration);
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public StudentRegistrationResponse rejectRegistration(int registrationId) {
        StudentRegistrationEntity registration = getPendingRegistrationOrThrow(registrationId);
        updateRegistration(registration, RegistrationStatus.REJECTED);
        return toResponse(registration);
    }
    
    /* ------------------------------------------------------ helpers ------------------------------------------------ */

    private StudentRegistrationResponse toResponse(StudentRegistrationEntity studentRegistration) {
        var actions = RegistrationActionPolicy.allowedActions(studentRegistration.getRegistrationStatus(), currentUserService.getCurrentUserRole());

        return StudentRegistrationMapper.toResponse(studentRegistration, actions);
    }

    private void updateRegistration(StudentRegistrationEntity registration, RegistrationStatus status) {
        registration.setRegistrationStatus(status);
        registration.setReviewedAt(Instant.now(clock));
        registration.setReviewedBy(currentUserService.getCurrentUser());
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
