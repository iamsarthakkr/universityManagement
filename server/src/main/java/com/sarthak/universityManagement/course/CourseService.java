package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.department.DepartmentService;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.instructor.InstructorService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseRepo courseRepo;
    private final InstructorService instructorService;
    private final DepartmentService departmentService;

    @Autowired
    public CourseService(CourseRepo courseRepo, InstructorService instructorService, DepartmentService departmentService) {
        this.courseRepo = courseRepo;
        this.instructorService = instructorService;
        this.departmentService = departmentService;
    }
    
    public List<CourseCatalogueResponse> getCoursesCatalogue() {
        var courses = courseRepo.findAllByOrderByDepartmentNameAsc();
        return CourseMapper.toCatalogue(courses);
    }
    
    @Transactional
    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public CourseResponse createCourse(CourseRequest courseRequest) {
        var instructorId = courseRequest.instructorId();
        var departmentId = courseRequest.departmentId();

        var instructor = instructorService.getInstructorById(instructorId);
        var department = departmentService.getDepartmentById(departmentId);

        var course = CourseMapper.toEntity(courseRequest);
        course.setInstructor(instructor);
        course.setDepartment(department);

        return CourseMapper.toResponse(courseRepo.save(course));
    }
    
}
