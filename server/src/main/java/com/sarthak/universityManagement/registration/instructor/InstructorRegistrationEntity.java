package com.sarthak.universityManagement.registration.instructor;

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

@Builder
@Entity
@Table(
    name = "instructor_registration",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_instructor_registration_username", columnNames = { "username" }),
        @UniqueConstraint(name = "unique_instructor_registration_email", columnNames = { "email" })
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstructorRegistrationEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "registrationStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId", nullable = false)
    private DepartmentEntity department;

    @Column(name = "reviewedAt")
    private Instant reviewedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewedBy")
    private UserEntity reviewedBy;
    
    @Column(name = "username", length = 50, nullable = false)
    private String username;
    
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "firstName", length = 50, nullable = false)
    private String firstName;
    
    @Column(name = "lastName", length = 50)
    private String lastName;
    
}
