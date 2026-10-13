package com.sarthak.universityManagement.seed.dto;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

/**
 * The shape of a seed file. Records refer to each other by natural keys (department code, course code,
 * username) or, for semesters and offerings, by a key that only exists inside the file.
 * Every date (semester dates, requestedAt, reviewedAt) is either an ISO date or an offset from the day the
 * seed is applied ("-10d", "+20d"). A missing requestedAt means the row is created "now".
 */
public record SeedFile(
    @NotNull Integer formatVersion,
    List<@Valid Department> departments,
    List<@Valid Instructor> instructors,
    List<@Valid Student> students,
    List<@Valid Course> courses,
    List<@Valid Semester> semesters,
    List<@Valid CourseOffering> courseOfferings,
    List<@Valid Enrollment> enrollments,
    List<@Valid StudentRegistration> studentRegistrations,
    List<@Valid InstructorRegistration> instructorRegistrations
) {
    // Bump when the shape changes; files written for another version are rejected instead of half-read.
    public static final int CURRENT_FORMAT_VERSION = 2;

    public SeedFile {
        departments = orEmpty(departments);
        instructors = orEmpty(instructors);
        students = orEmpty(students);
        courses = orEmpty(courses);
        semesters = orEmpty(semesters);
        courseOfferings = orEmpty(courseOfferings);
        enrollments = orEmpty(enrollments);
        studentRegistrations = orEmpty(studentRegistrations);
        instructorRegistrations = orEmpty(instructorRegistrations);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    public record Department(
        @NotBlank String code,
        @NotBlank String name
    ) {}

    public record Instructor(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String department,
        @NotBlank String firstName,
        String lastName,
        String phoneNumber,
        @Valid Registration registration
    ) {}

    public record Student(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String department,
        @NotBlank String firstName,
        String lastName,
        @NotNull LocalDate dateOfBirth,
        String phoneNumber,
        String address,
        String fatherName,
        String motherName,
        @Valid Registration registration
    ) {}

    // On a student or instructor: the account came from an APPROVED request, created from the profile itself
    public record Registration(
        @NotBlank String requestedAt,
        @NotBlank String reviewedAt
    ) {}

    public record Course(
        @NotBlank String code,
        @NotBlank String department,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Integer credits
    ) {}

    // year is not in the file: it is taken from the resolved startDate, so relative dates never drift from it
    public record Semester(
        @NotBlank String key,
        @NotNull SemesterTerm term,
        @NotNull SemesterStatus status,
        @NotBlank String registrationStartDate,
        @NotBlank String registrationEndDate,
        @NotBlank String startDate,
        @NotBlank String endDate
    ) {}

    public record CourseOffering(
        @NotBlank String key,
        @NotBlank String course,
        @NotBlank String semester,
        @NotBlank String instructor,
        @NotBlank String section,
        @NotNull @Positive Integer capacity
    ) {}

    public record Enrollment(
        @NotBlank String student,
        @NotBlank String offering,
        @NotNull EnrollmentStatus status,
        String requestedAt
    ) {}

    /*
     * Requests that never became an account: PENDING (for the admin's review screen) or REJECTED (with reviewedAt).
     * APPROVED ones are written as a "registration" on the student or instructor instead, so they always match a user.
     */
    public record StudentRegistration(
        @NotNull RegistrationStatus status,
        String requestedAt,
        String reviewedAt,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String department,
        @NotBlank String firstName,
        String lastName,
        @NotNull LocalDate dateOfBirth
    ) {}

    public record InstructorRegistration(
        @NotNull RegistrationStatus status,
        String requestedAt,
        String reviewedAt,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String department,
        @NotBlank String firstName,
        String lastName
    ) {}
}
