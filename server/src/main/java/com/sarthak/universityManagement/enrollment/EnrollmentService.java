package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.courseOffering.CourseOfferingActionPolicy;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.courseOffering.types.EnrollDenial;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentFilter;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.security.annotation.AdminOrCourseOfferingInstructor;
import com.sarthak.universityManagement.security.annotation.CanAccessEnrollment;
import com.sarthak.universityManagement.security.annotation.CurrentStudent;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class EnrollmentService {
    private final EnrollmentRepo enrollmentRepo;
    private final CourseOfferingService courseOfferingService;
    private final StudentService studentService;
    private final CurrentUserService currentUserService;
    private final Clock clock;

    @Autowired
    public EnrollmentService(
        EnrollmentRepo enrollmentRepo,
        CourseOfferingService courseOfferingService,
        StudentService studentService,
        CurrentUserService currentUserService,
        Clock clock
    ) {
        this.enrollmentRepo = enrollmentRepo;
        this.courseOfferingService = courseOfferingService;
        this.studentService = studentService;
        this.currentUserService = currentUserService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @CanAccessEnrollment
    public EnrollmentResponse getEnrollment(Integer enrollmentId) {
        return getEnrollmentResponse(getEnrollmentOrThrow(enrollmentId));
    }

    @Transactional(readOnly = true)
    @PreAuthorize(AuthorizationExpressions.STUDENT)
    public List<EnrollmentResponse> getEnrollmentsForStudent(EnrollmentFilter filter) {
        var currentStudentId = currentUserService.getCurrentStudent().getId();
        return enrollmentRepo
            .searchForStudent(currentStudentId, filter)
            .stream()
            .map(this::getEnrollmentResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    @AdminOrCourseOfferingInstructor
    public List<EnrollmentResponse> getEnrollmentsForCourseOffering(Integer courseOfferingId, EnrollmentFilter filter) {
        return enrollmentRepo
            .searchForOffering(courseOfferingId, filter)
            .stream()
            .map(this::getEnrollmentResponse)
            .toList();
    }

    @Transactional
    @CurrentStudent
    public EnrollmentResponse createEnrollment(Integer studentId, Integer courseOfferingId) {
        var student = studentService.getStudentEntity(studentId);
        var courseOffering = courseOfferingService.getCourseOfferingEntity(courseOfferingId);

        validateEnrollmentRequest(courseOffering, student);

        EnrollmentEntity toSave = EnrollmentEntity.builder()
            .student(student)
            .courseOffering(courseOffering)
            .status(EnrollmentStatus.PENDING)
            .build();

        return getEnrollmentResponse(enrollmentRepo.save(toSave));
    }

    @CanAccessEnrollment
    @Transactional
    public EnrollmentResponse performAction(Integer enrollmentId, EnrollmentAction action) {
        var enrollment = getEnrollmentForUpdateOrThrow(enrollmentId);
        // should be locked before performing validation as the validator checks the offering as well
        var courseOffering = courseOfferingService.getCourseOfferingForEnrollment(enrollment.getCourseOffering().getId());

        var currentUser = currentUserService.getCurrentUserPrincipal();
        EnrollmentActionPolicy
            .validate(enrollment, action, currentUser)
            .ifPresent(denial -> { throw denial.toException(enrollmentId); });

        var targetStatus = action.getTargetStatus();
        if(action.equals(EnrollmentAction.APPROVE)) {
            courseOffering.enroll();
        }
        if(action.equals(EnrollmentAction.DROP)) {
            courseOffering.releaseEnrolled();
        }
        enrollment.setStatus(targetStatus);

        return getEnrollmentResponse(enrollmentRepo.saveAndFlush(enrollment));
    }

    /* ---------------------------------------------------------------------------------------------------------------*/

    private void validateEnrollmentRequest(CourseOfferingEntity courseOffering, StudentEntity student) {
        CourseOfferingActionPolicy
            .validateEnroll(courseOffering, currentUserService.getCurrentUserRole(), LocalDate.now(clock))
            .ifPresent(denial -> { throw denial.toException(courseOffering.getId()); });
        if(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student.getId(), courseOffering.getId())) {
            throw EnrollDenial.ALREADY_REQUESTED.toException(courseOffering.getId());
        }
    }

    private EnrollmentEntity getEnrollmentOrThrow(Integer enrollmentId) {
        return enrollmentRepo
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + enrollmentId + " not found"));
    }

    private EnrollmentEntity getEnrollmentForUpdateOrThrow(Integer enrollmentId) {
        return enrollmentRepo
            .findForUpdateById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + enrollmentId + " not found"));
    }

    private EnrollmentResponse getEnrollmentResponse(EnrollmentEntity enrollment) {
        var currentUserPrincipal = currentUserService.getCurrentUserPrincipal();
        var actions = EnrollmentActionPolicy.allowedActions(enrollment, currentUserPrincipal);
        return EnrollmentMapper.toResponse(enrollment, LocalDate.now(clock), actions);
    }

}
