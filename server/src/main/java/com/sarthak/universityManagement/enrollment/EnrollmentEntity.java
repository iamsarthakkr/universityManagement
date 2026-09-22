package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.entity.BaseEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.student.StudentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "enrollment",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_enrollment", columnNames = {"studentId", "courseOfferingId"})
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studentId", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseOfferingId", nullable = false)
    private CourseOfferingEntity courseOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status;

    public boolean canTransitionTo(EnrollmentStatus newStatus) {
        return switch (newStatus) {
            case PENDING -> false;
            case ENROLLED, CANCELLED, REJECTED -> status == EnrollmentStatus.PENDING;
            case DROPPED -> status == EnrollmentStatus.ENROLLED;
        };
    }

}
