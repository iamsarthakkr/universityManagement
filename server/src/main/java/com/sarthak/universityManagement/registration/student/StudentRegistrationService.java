package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.department.DepartmentService;
import com.sarthak.universityManagement.registration.RegistrationActionPolicy;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationRequest;
import com.sarthak.universityManagement.registration.student.dto.StudentRegistrationResponse;
import com.sarthak.universityManagement.registration.dto.RegistrationFilter;
import com.sarthak.universityManagement.registration.types.RegistrationAction;
import com.sarthak.universityManagement.student.StudentService;
import com.sarthak.universityManagement.user.CurrentUserService;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.user.UserService;
import com.sarthak.universityManagement.registration.validators.RegistrationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class StudentRegistrationService {
    private final RegistrationValidator registrationValidator;
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final DepartmentService departmentService;
    private final Clock clock;

    @Autowired
    public StudentRegistrationService(
        RegistrationValidator registrationValidator,
        StudentRegistrationRepo studentRegistrationRepo,
        PasswordEncoder passwordEncoder,
        UserService userService,
        StudentService studentService,
        CurrentUserService currentUserService,
        DepartmentService departmentService,
        Clock clock
    ) {
        this.registrationValidator = registrationValidator;
        this.studentRegistrationRepo = studentRegistrationRepo;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.studentService = studentService;
        this.currentUserService = currentUserService;
        this.departmentService = departmentService;
        this.clock = clock;
    }

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
    public List<StudentRegistrationResponse> getRequests(RegistrationFilter filter) {
        return studentRegistrationRepo
            .search(filter)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public StudentRegistrationResponse performAction(int registrationId, RegistrationAction action) {
        var registration = getRegistrationForUpdate(registrationId);
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
            case APPROVE ->  approveRegistration(registration);
            case REJECT -> rejectRegistration(registration);
        };
    }

    /* ------------------------------------------------------ helpers ------------------------------------------------ */

    private StudentRegistrationEntity getRegistrationForUpdate(int registrationId) {
        return studentRegistrationRepo
            .findForUpdateById(registrationId)
            .orElseThrow(() -> new ResourceNotFoundException("Student registration not found with id " + registrationId));
    }

    private StudentRegistrationResponse approveRegistration(StudentRegistrationEntity registration) {
        // create user
        UserEntity user = userService.createUserForRegistration(StudentRegistrationMapper.toCreateUserCommand(registration));

        // create associated student
        studentService.createStudentForUser(StudentRegistrationMapper.toCreateStudentCommand(registration), user);

        // approve registration
        updateRegistration(registration, RegistrationStatus.APPROVED);
        return toResponse(registration);
    }

    private StudentRegistrationResponse rejectRegistration(StudentRegistrationEntity registration) {
        updateRegistration(registration, RegistrationStatus.REJECTED);
        return toResponse(registration);
    }

    private void updateRegistration(StudentRegistrationEntity registration, RegistrationStatus status) {
        registration.setRegistrationStatus(status);
        registration.setReviewedAt(Instant.now(clock));
        registration.setReviewedBy(currentUserService.getCurrentUser());
    }

    private StudentRegistrationResponse toResponse(StudentRegistrationEntity studentRegistration) {
        var actions = RegistrationActionPolicy.allowedActions(studentRegistration.getRegistrationStatus(), currentUserService.getCurrentUserRole());

        return StudentRegistrationMapper.toResponse(studentRegistration, actions);
    }

}
