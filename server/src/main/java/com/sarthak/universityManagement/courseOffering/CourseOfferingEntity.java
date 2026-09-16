package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.entity.BaseEntity;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.semester.SemesterEntity;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "course_offering",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_course_offering", columnNames = {"courseId", "semesterId", "section"})
    },
    check = {
        @CheckConstraint(name = "chk_course_offering_capacity", constraint = "capacity > 0")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseOfferingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId", nullable = false)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructorId", nullable = false)
    private InstructorEntity instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semesterId", nullable = false)
    private SemesterEntity semester;

    @Column(name = "section", length = 10, nullable = false)
    private String section;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

}
