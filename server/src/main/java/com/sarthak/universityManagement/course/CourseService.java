package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.department.DepartmentService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CourseService {
    private final CourseRepo courseRepo;
    private final DepartmentService departmentService;

    @Autowired
    public CourseService(CourseRepo courseRepo, DepartmentService departmentService) {
        this.courseRepo = courseRepo;
        this.departmentService = departmentService;
    }

    @Transactional(readOnly = true)
    public List<CourseCatalogueResponse> getCoursesCatalogue() {
        var courses = courseRepo.findAllByOrderByDepartmentNameAsc();
        return CourseMapper.toCatalogue(courses);
    }

    @Transactional(readOnly = true)
    public CourseEntity getCourseEntity(Integer courseId) {
        return  courseRepo
            .findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course with id: " + courseId + " not found!"));
    }

    @PreAuthorize(AuthorizationExpressions.ADMIN)
    public CourseResponse createCourse(CourseRequest courseRequest) {
        var departmentId = courseRequest.departmentId();

        var department = departmentService.getDepartmentById(departmentId);
        var course = CourseMapper.toEntity(courseRequest);
        course.setDepartment(department);

        return CourseMapper.toResponse(courseRepo.save(course));
    }

}
