package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    private final CourseRepo courseRepo;
    private final InstructorRepo instructorRepo;
    
    @Autowired
    public CourseService(CourseRepo courseRepo, InstructorRepo instructorRepo) {
        this.courseRepo = courseRepo;
        this.instructorRepo = instructorRepo;
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public CourseResponse createCourse(CourseRequest courseRequest) {
        var instructorId = courseRequest.instructorId();
        var instructor = instructorRepo
            .findById(courseRequest.instructorId())
            .orElseThrow(() -> new BadRequestException("Instructor does not exist with id " + instructorId));
        
        var course = CourseMapper.toEntity(courseRequest);
        course.setInstructor(instructor);
        return CourseMapper.toResponse(courseRepo.save(course));
    }
}
