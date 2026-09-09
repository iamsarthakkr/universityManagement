package com.sarthak.universityManagement.semester;

import com.sarthak.universityManagement.common.entity.BaseEntity;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import jakarta.persistence.CheckConstraint;
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

import java.time.LocalDate;
import java.time.Year;

@Entity
@Table(
    name = "semester",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_semester_term_year", columnNames = {"term", "year"})
    },
    check = {
        @CheckConstraint(name = "chk_semester_dates", constraint = "registrationStartDate < registrationEndDate AND startDate < endDate")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SemesterEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "term", nullable = false)
    private SemesterTerm term;

    @Column(name = "year", nullable = false)
    private Year year;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SemesterStatus status;

    @Column(name = "registrationStartDate", nullable = false)
    private LocalDate registrationStartDate;

    @Column(name = "registrationEndDate", nullable = false)
    private LocalDate registrationEndDate;

    @Column(name = "startDate", nullable = false)
    private LocalDate startDate;

    @Column(name = "endDate", nullable = false)
    private LocalDate endDate;
}
