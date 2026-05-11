package com.sarthak.universityEnrollmentManagement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Entity
@Table(
    name = "student",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user", columnNames = { "user_id" })
    }
)
@Getter
@Setter
public class StudentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "firstName", length = 50, nullable = false)
    private String firstName;

    @Column(name = "lastName", length = 50)
    private String lastName;
    
    @Column(name = "phoneNumber", length = 10, nullable = false)
    private String phoneNumber;
    
    @Column(name = "dateOfBirth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Column(name = "address", length = 100, nullable = false)
    private String address;
    
    @Column(name = "fatherName", length = 50, nullable = false)
    private String fatherName;
    
    @Column(name = "motherName", length = 50, nullable = false)
    private String motherName;
    
}
