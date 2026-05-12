package com.sarthak.universityEnrollmentManagement.entity;

import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    name = "instructor_registration_requests",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_instructor_registration", columnNames = { "username" }),
        @UniqueConstraint(name = "unique_instructor_email", columnNames = { "email" })
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
    
    @Column(name = "registration_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;
    
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
    
    @Column(name = "phoneNumber", length = 10, nullable = false)
    private String phoneNumber;
    
    @Column(name = "department", length = 50, nullable = false)
    private String department;
}
