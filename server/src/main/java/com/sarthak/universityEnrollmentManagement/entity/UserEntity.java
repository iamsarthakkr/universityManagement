package com.sarthak.universityEnrollmentManagement.entity;

import com.sarthak.universityEnrollmentManagement.entity.types.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_user", columnNames = { "username" }),
        @UniqueConstraint(name = "unique_user_email", columnNames = { "email" })
    }
)
@Getter
@Setter
public class UserEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "username", length = 50, nullable = false)
    private String username;
    
    @Column(name = "password", length = 200, nullable = false)
    private String password;
    
    @Column(name = "email", length = 100, nullable = false)
    private String email;
    
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
}
