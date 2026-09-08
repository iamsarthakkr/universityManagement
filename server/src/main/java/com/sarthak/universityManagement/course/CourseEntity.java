package com.sarthak.universityManagement.course;

import com.sarthak.universityManagement.common.entity.BaseEntity;
import com.sarthak.universityManagement.department.DepartmentEntity;
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

@Builder
@Entity
@Table(
    name = "courses",
    check = {
        @CheckConstraint(name = "valid_credits", constraint = "credits > 0 AND credits < 10"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_course_code", columnNames = "code")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department", nullable = false)
    private DepartmentEntity department;
    
    @Column(name = "code", nullable = false)
    private String code;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "credits", nullable = false)
    private Integer credits;

}
