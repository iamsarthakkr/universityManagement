package com.sarthak.universityManagement.registration.instructor;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.department.DepartmentService;
import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.registration.RegistrationActionPolicy;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationRequest;
import com.sarthak.universityManagement.registration.instructor.dto.InstructorRegistrationResponse;
import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.user.CurrentUserService;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.user.UserService;
import com.sarthak.universityManagement.registration.validators.RegistrationValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class InstructorRegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final RegistrationValidator registrationValidator;
    private final UserService userService;
    private final InstructorService instructorService;
    private final CurrentUserService currentUserService;
    private final DepartmentService departmentService;
    private final Clock clock;

    public InstructorRegistrationService(
        PasswordEncoder passwordEncoder,
        InstructorRegistrationRepo instructorRegistrationRepo,
        RegistrationValidator registrationValidator,
        UserService userService,
        InstructorService instructorService,
        CurrentUserService currentUserService,
        DepartmentService departmentService,
        Clock clock
    ) {
        this.passwordEncoder = passwordEncoder;
        this.instructorRegistrationRepo = instructorRegistrationRepo;
        this.registrationValidator = registrationValidator;
        this.userService = userService;
        this.instructorService = instructorService;
        this.currentUserService = currentUserService;
        this.departmentService = departmentService;
        this.clock = clock;
    }

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
    public List<InstructorRegistrationResponse> getRequests(RegistrationFilter filter) {
        return instructorRegistrationRepo
            .search(filter)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public InstructorRegistrationResponse performAction(Integer registrationId, RegistrationAction action) {
        var registration = getRegistrationForUpdateOrThrow(registrationId);
        var currentUserRole = currentUserService.getCurrentUserRole();

        if(!RegistrationActionPolicy.canPerform(action, registration.getRegistrationStatus(), currentUserRole)) {
            throw new ConflictException(
                "Invalid registration transition: "
                    + registration.getRegistrationStatus()
                    + " -> "
                    + action.getTargetStatus()
            );
        }

        return switch (action) {
            case APPROVE -> approveRegistration(registration);
            case REJECT -> rejectRegistration(registration);
        };
    }


    /* ------------------------------------------------------ helpers ------------------------------------------------ */
    private InstructorRegistrationEntity getRegistrationForUpdateOrThrow(int registrationId) {
        return instructorRegistrationRepo
            .findForUpdateById(registrationId)
            .orElseThrow(() -> new ResourceNotFoundException("Instructor registration not found with id " + registrationId));
    }

    private InstructorRegistrationResponse approveRegistration(InstructorRegistrationEntity registration) {
        // create user
        UserEntity user = userService.createUserForRegistration(InstructorRegistrationMapper.toCreateUserCommand(registration));

        // create associated instructor
        instructorService.createInstructorForUser(InstructorRegistrationMapper.toCreateInstructorCommand(registration), user);

        // approve registration
        updateRegistration(registration, RegistrationStatus.APPROVED);

        return toResponse(registration);
    }

    private InstructorRegistrationResponse rejectRegistration(InstructorRegistrationEntity registration) {
        updateRegistration(registration, RegistrationStatus.REJECTED);
        return toResponse(registration);
    }

    private void updateRegistration(InstructorRegistrationEntity registration, RegistrationStatus status) {
        registration.setRegistrationStatus(status);
        registration.setReviewedAt(Instant.now(clock));
        registration.setReviewedBy(currentUserService.getCurrentUser());
    }

    private InstructorRegistrationResponse toResponse(InstructorRegistrationEntity registration) {
        var actions = RegistrationActionPolicy.allowedActions(registration.getRegistrationStatus(), currentUserService.getCurrentUserRole());
        return InstructorRegistrationMapper.toResponse(registration, actions);
    }

}
