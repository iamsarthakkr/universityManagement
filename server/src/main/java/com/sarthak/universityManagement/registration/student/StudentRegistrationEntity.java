package com.sarthak.universityManagement.registration.student;

import com.sarthak.universityManagement.common.entity.BaseEntity;
import com.sarthak.universityManagement.common.types.RegistrationStatus;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.user.UserEntity;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@Entity
@Table(
    name = "student_registration_requests",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_student_registration", columnNames = { "username" }),
        @UniqueConstraint(name = "unique_student_registration_email", columnNames = { "email" })
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "registration_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;
    
    @Column(name = "reviewed_at")
    private Instant reviewedAt;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserEntity reviewedBy;
    
    @Column(name = "username", length = 50, nullable = false)
    private String username;
    
    @Column(name = "password", length = 200, nullable = false)
    private String password;
    
    @Column(name = "firstName", length = 50, nullable = false)
    private String firstName;
    
    @Column(name = "lastName", length = 50)
    private String lastName;
    
    @Column(name = "email", length = 100, nullable = false)
    private String email;
    
    @Column(name = "dateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department", nullable = false)
    private DepartmentEntity department;
    
}
