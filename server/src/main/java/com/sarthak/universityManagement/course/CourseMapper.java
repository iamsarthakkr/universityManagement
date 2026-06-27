package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.course.dto.CourseCatalogueResponse;
import com.sarthak.universityManagement.course.dto.CourseRequest;
import com.sarthak.universityManagement.course.dto.CourseResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseMapper {
    public static CourseEntity toEntity(CourseRequest courseRequest) {
        return CourseEntity
            .builder()
            .department(courseRequest.department())
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
            .department(courseEntity.getDepartment())
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
        Map<String, List<CourseEntity>> coursesMap = new HashMap<>();
        courses.forEach(course -> {
            var department = course.getDepartment();
            if(!coursesMap.containsKey(department)) {
                coursesMap.put(department, new ArrayList<>());
            }
            coursesMap.get(department).add(course);
        });
        
        List<CourseCatalogueResponse> ret = new ArrayList<>();
        coursesMap.forEach((key, courseList) -> ret.add(new CourseCatalogueResponse(key, courseList.stream().map(CourseMapper::toResponse).toList())));
        return ret;
    }
}
