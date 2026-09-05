package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;
import com.sarthak.universityManagement.department.DepartmentMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseMapper {
    public static CourseEntity toEntity(CourseRequest courseRequest) {
        return CourseEntity
            .builder()
            .code(courseRequest.code())
            .title(courseRequest.title())
            .description(courseRequest.description())
            .credits(courseRequest.credits())
            .capacity(courseRequest.capacity())
            .build();
    }

    public static CourseResponse toResponse(CourseEntity courseEntity) {
        return CourseResponse
            .builder()
            .courseId(courseEntity.getId())
            .departmentId(courseEntity.getDepartment().getId())
            .departmentName(courseEntity.getDepartment().getName())
            .code(courseEntity.getCode())
            .title(courseEntity.getTitle())
            .description(courseEntity.getDescription())
            .credits(courseEntity.getCredits())
            .capacity(courseEntity.getCapacity())
            .instructorId(courseEntity.getInstructor().getId())
            .instructor(courseEntity.getInstructor().getFirstName())
            .build();
    }
    
    public static List<CourseCatalogueResponse> toCatalogue(List<CourseEntity> courses) {
        Map<Integer, List<CourseEntity>> coursesMap = new HashMap<>();
        Map<Integer, String> departmentNameMap = new HashMap<>();
        courses.forEach(course -> {
            var department = course.getDepartment();
            if(!coursesMap.containsKey(department.getId())) {
                coursesMap.put(department.getId(), new ArrayList<>());
                departmentNameMap.put(department.getId(), department.getName());
            }
            coursesMap.get(department.getId()).add(course);
        });
        
        List<CourseCatalogueResponse> ret = new ArrayList<>();
        coursesMap.forEach((key, courseList) ->
                ret.add(new CourseCatalogueResponse(key, departmentNameMap.get(key),  courseList.stream().map(CourseMapper::toResponse).toList()))
        );
        return ret;
    }
}
