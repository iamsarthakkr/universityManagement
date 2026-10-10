package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.dto.SemesterResponse;
import com.sarthak.universityManagement.semester.types.SemesterAction;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.validators.SemesterValidator;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SemesterService {

    private final SemesterRepo semesterRepo;
    private final Clock clock;
    private final CurrentUserService currentUserService;

    @Autowired
    public SemesterService(
        SemesterRepo semesterRepo,
        Clock clock,
        CurrentUserService currentUserService
    ) {
        this.semesterRepo = semesterRepo;
        this.clock = clock;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public SemesterEntity getSemesterEntity(Integer semesterId) {
        return getSemesterOrThrow(semesterId);
    }

    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public SemesterResponse createSemester(CreateSemesterRequest semesterRequest) {
        if(semesterRepo.existsByTermAndYear(semesterRequest.term(), semesterRequest.year())) {
            throw new ConflictException("Semester already exists for "
                + semesterRequest.term() + " "  + semesterRequest.year());
        }
        SemesterValidator.validateSemesterDates(semesterRequest);
        var entity = SemesterMapper.toEntity(semesterRequest);
        entity.setStatus(SemesterStatus.PLANNED);

        return getSemesterResponse(semesterRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getSemesters() {
        return semesterRepo
            .findAllByOrderByYearAscTermDesc()
            .stream()
            .map(this::getSemesterResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemester(Integer semesterId) {
        return getSemesterResponse(getSemesterOrThrow(semesterId));
    }

    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public SemesterResponse transition(Integer semesterId, SemesterAction semesterAction) {
        var semester = getSemesterOrThrow(semesterId);
        var actorRole = currentUserService.getCurrentUserRole();

        var targetStatus = semesterAction.getTargetStatus();
        if(!SemesterActionPolicy.canPerform(semester, semesterAction, actorRole)) {
            throw new BadRequestException(
                "Invalid semester transition: "
                    + semester.getStatus()
                    + " -> "
                    + targetStatus
            );
        }

        semester.setStatus(targetStatus);
        return getSemesterResponse(semester);
    }

    /* ------------------------------------------------------ helpers ------------------------------------------------ */

    private SemesterEntity getSemesterOrThrow(Integer semesterId) {
        return semesterRepo
            .findById(semesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id " + semesterId));
    }

    private SemesterResponse getSemesterResponse(SemesterEntity semester) {
        var actorRole = currentUserService.getCurrentUserRole();
        var actions = SemesterActionPolicy.allowedActions(semester, actorRole);

        return SemesterMapper.toResponse(semester, LocalDate.now(clock), actions);
    }
}
