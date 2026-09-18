package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.course.CourseRepo;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.atomic.AtomicInteger;

@TestComponent
@Profile("test")
public class CourseSeeder {
    private final CourseRepo courseRepo;
    private final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    public CourseSeeder(CourseRepo courseRepo) {
        this.courseRepo = courseRepo;
    }

    public CourseEntity save(CourseEntity course) {
        return courseRepo.saveAndFlush(course);
    }

    public CourseEntity saveDefault(DepartmentEntity department) {
        var cnt =  counter.incrementAndGet();
        return courseRepo.saveAndFlush(
            CourseFixtures
                .course(department)
                .code("code-" + cnt)
                .build()
        );
    }

    public CourseEntity seedOrGet(DepartmentEntity department, String code) {
        var existing = courseRepo.findByCode(code);
        if(existing.isPresent()) {
            var course = existing.get();
            if(!course.getDepartment().getId().equals(department.getId())) {
                throw new BadRequestException("Department with code " + code + " already exists in different department");
            }
            return course;
        }

        return save(
            CourseFixtures.course(department)
                .code(code)
                .build()
        );
    }

}
