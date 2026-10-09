package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.security.annotation.AdminOrCourseOfferingInstructor;
import com.sarthak.universityManagement.security.annotation.CanAccessEnrollment;
import com.sarthak.universityManagement.security.annotation.CurrentStudent;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import com.sarthak.universityManagement.user.CurrentUserService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
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

    @CanAccessEnrollment
    public EnrollmentResponse getEnrollment(Integer enrollmentId) {
        return getEnrollmentResponse(getEnrollmentOrThrow(enrollmentId));
    }

    @CurrentStudent
    public List<EnrollmentResponse> getEnrollmentsForStudent(Integer studentId, @Nullable Integer semesterId, @Nullable EnrollmentStatus enrollmentStatus) {
        return enrollmentRepo
            .findByStudentId(studentId, semesterId, enrollmentStatus)
            .stream()
            .map(this::getEnrollmentResponse)
            .toList();
    }

    @AdminOrCourseOfferingInstructor
    public List<EnrollmentResponse> getEnrollmentsForCourseOffering(Integer courseOfferingId, @Nullable EnrollmentStatus status) {
        return enrollmentRepo
            .findAllForCourseOfferingWithDetails(courseOfferingId, status)
            .stream()
            .map(this::getEnrollmentResponse)
            .toList();
    }

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
        if(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student.getId(), courseOffering.getId())) {
            throw new BadRequestException("Enrollment already exists for student in the course offering");
        }
        var semester = courseOffering.getSemester();
        if(!semester.isRegistrationOpen(LocalDate.now(clock))) {
            throw new BadRequestException("Registration is not open");
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
