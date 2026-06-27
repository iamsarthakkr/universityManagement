package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseRepo courseRepo;
    private final InstructorRepo instructorRepo;
    
    @Autowired
    public CourseService(CourseRepo courseRepo, InstructorRepo instructorRepo) {
        this.courseRepo = courseRepo;
        this.instructorRepo = instructorRepo;
    }
    
    public List<CourseCatalogueResponse> getCoursesCatalogue() {
        var courses = courseRepo.findAllByOrderByDepartmentAsc();
        return CourseMapper.toCatalogue(courses);
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
