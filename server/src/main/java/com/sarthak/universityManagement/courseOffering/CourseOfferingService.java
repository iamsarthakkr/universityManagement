package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.course.CourseService;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.courseOffering.dto.CreateCourseOfferingRequest;
import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.instructor.validators.InstructorValidator;
import com.sarthak.universityManagement.semester.SemesterService;
import com.sarthak.universityManagement.semester.validators.SemesterValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseOfferingService {
    private final CourseOfferingRepo courseOfferingRepo;
    private final CourseService courseService;
    private final InstructorService instructorService;
    private final SemesterService semesterService;

    @Autowired
    public CourseOfferingService(
        CourseOfferingRepo courseOfferingRepo,
        CourseService courseService,
        InstructorService instructorService,
        SemesterService semesterService
    ) {
        this.courseOfferingRepo = courseOfferingRepo;
        this.courseService = courseService;
        this.instructorService = instructorService;
        this.semesterService = semesterService;
    }

    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public CourseOfferingResponse createOffering(CreateCourseOfferingRequest courseOfferingRequest) {
        var courseId = courseOfferingRequest.courseId();
        var instructorId = courseOfferingRequest.instructorId();
        var semesterId = courseOfferingRequest.semesterId();
        var section = courseOfferingRequest.section();

        var course = courseService.getCourseEntity(courseId);
        var instructor = instructorService.getInstructorEntity(instructorId);
        var semester =  semesterService.getSemesterEntity(semesterId);

        SemesterValidator.validateSemesterAllowsOfferings(semester);
        InstructorValidator.validateInstructorForCourse(course, instructor);
        if(courseOfferingRepo.existsByCourseIdAndSemesterIdAndSection(courseId, semesterId, section)) {
            throw new BadRequestException("course offering already exists");
        }

        var entity = CourseOfferingMapper.toEntity(courseOfferingRequest);
        entity.setCourse(course);
        entity.setInstructor(instructor);
        entity.setSemester(semester);
        return CourseOfferingMapper.toResponse(courseOfferingRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public CourseOfferingResponse getOffering(Integer offeringId) {
        var entity = courseOfferingRepo
            .findById(offeringId)
            .orElseThrow(() -> new ResourceNotFoundException("Offering not found with id " + offeringId));

        return CourseOfferingMapper.toResponse(entity);
    }

    public CourseOfferingEntity getCourOfferingEntity(Integer offeringId) {
        return courseOfferingRepo
            .findById(offeringId)
            .orElseThrow(()  -> new ResourceNotFoundException("Offering not found with id " + offeringId));
    }

    public CourseOfferingEntity getCourseOfferingForEnrollment(Integer offeringId) {
        return courseOfferingRepo
            .findForUpdateById(offeringId)
            .orElseThrow(()  -> new ResourceNotFoundException("Offering not found with id " + offeringId));
    }

    @Transactional(readOnly = true)
    public List<CourseOfferingResponse> getOfferingsBySemester(Integer semesterId) {
        return courseOfferingRepo
            .findAllBySemesterId(semesterId)
            .stream()
            .map(CourseOfferingMapper::toResponse)
            .toList();
    }

}
