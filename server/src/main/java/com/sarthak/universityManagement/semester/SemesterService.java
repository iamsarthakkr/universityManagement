package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.semester.dto.CreateSemesterRequest;
import com.sarthak.universityManagement.semester.dto.SemesterResponse;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SemesterService {

    private final SemesterRepo semesterRepo;

    @Autowired
    public SemesterService(SemesterRepo semesterRepo) {
        this.semesterRepo = semesterRepo;
    }

    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public SemesterResponse createSemester(CreateSemesterRequest semesterRequest) {
        var entity = SemesterMapper.toEntity(semesterRequest);
        entity.setStatus(SemesterStatus.PLANNED);

        return SemesterMapper.toResponse(semesterRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getSemesters() {
        return semesterRepo
            .findAll()
            .stream()
            .map(SemesterMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemester(Integer semesterId) {
        return SemesterMapper.toResponse(getSemesterOrThrow(semesterId));
    }

    @Transactional(readOnly = true)
    public SemesterEntity getSemesterEntity(Integer semesterId) {
        return getSemesterOrThrow(semesterId);
    }

    public void transition(Integer semesterId, SemesterStatus newStatus) {
        var semester = getSemesterOrThrow(semesterId);

        if(!semester.canTransitionTo(SemesterStatus.CANCELLED)) {
            throw new BadRequestException("Semester can't be " + newStatus.toString());
        }

        semester.setStatus(newStatus);
    }

    private SemesterEntity getSemesterOrThrow(Integer semesterId) {
        return semesterRepo
            .findById(semesterId)
            .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id " + semesterId));
    }

}
