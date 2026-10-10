package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentDenial;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenario;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenarioSeeder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentActionPolicyIntegrationTests extends IntegrationTests {
    enum TestActor { ADMIN, OFFERING_INSTRUCTOR, OTHER_INSTRUCTOR, OWNING_STUDENT, OTHER_STUDENT }

    private static final int CAPACITY = 10;
    private static final int SEATS_AVAILABLE = 5;
    private static final int OFFERING_FULL = CAPACITY;

    @Autowired
    private EnrollmentScenarioSeeder enrollmentScenarioSeeder;
    @Autowired
    private Clock clock;

    private EnrollmentScenario seedTarget(EnrollmentStatus status, int enrolled, SemesterStatus semesterStatus) {
        return enrollmentScenarioSeeder
            .builder()
            .enrollmentStatus(status)
            .student(s -> s.studentNumber(1))
            .offering(o -> o
                .capacity(CAPACITY)
                .enrolled(enrolled)
                .courseNumber(1)
                .instructorNumber(1)
                .semester(s -> s.status(semesterStatus).registrationOpenOn(LocalDate.now(clock))))
            .build();
    }

    private EnrollmentScenario seedOther() {
        return enrollmentScenarioSeeder
            .builder()
            .student(s -> s.studentNumber(2))
            .offering(o -> o.courseNumber(2).instructorNumber(2))
            .build();
    }

    private UserPrincipal getUserFor(TestActor actor, EnrollmentEntity target, EnrollmentEntity other) {
        var base = UserFixtures.userPrincipal();
        var targetStudent = target.getStudent();
        var otherStudent = other.getStudent();
        var targetInstructor = target.getCourseOffering().getInstructor();
        var otherInstructor = other.getCourseOffering().getInstructor();
        return switch (actor) {
            case ADMIN -> base.role(Role.ADMIN).userId(-1).build();
            case OWNING_STUDENT -> base.role(Role.STUDENT).userId(targetStudent.getUser().getId()).build();
            case OFFERING_INSTRUCTOR -> base.role(Role.INSTRUCTOR).userId(targetInstructor.getUser().getId()).build();
            case OTHER_STUDENT -> base.role(Role.STUDENT).userId(otherStudent.getUser().getId()).build();
            case OTHER_INSTRUCTOR -> base.role(Role.INSTRUCTOR).userId(otherInstructor.getUser().getId()).build();
        };
    }

    @Nested
    class AllowedActions {
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.PENDING, List.of(EnrollmentAction.APPROVE, EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.ENROLLED, List.of()),
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.REJECTED, List.of()),
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.CANCELLED, List.of()),
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.DROPPED, List.of()),

                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.PENDING, List.of(EnrollmentAction.APPROVE, EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.ENROLLED, List.of()),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.REJECTED, List.of()),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.CANCELLED, List.of()),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.DROPPED, List.of()),

                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.PENDING, List.of(EnrollmentAction.CANCEL)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.ENROLLED, List.of(EnrollmentAction.DROP)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.REJECTED, List.of()),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.CANCELLED, List.of()),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.DROPPED, List.of()),

                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, EnrollmentStatus.PENDING, List.of()),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, EnrollmentStatus.ENROLLED, List.of()),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, EnrollmentStatus.REJECTED, List.of()),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, EnrollmentStatus.CANCELLED, List.of()),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, EnrollmentStatus.DROPPED, List.of()),

                Arguments.arguments(TestActor.OTHER_STUDENT, EnrollmentStatus.PENDING, List.of()),
                Arguments.arguments(TestActor.OTHER_STUDENT, EnrollmentStatus.ENROLLED, List.of()),
                Arguments.arguments(TestActor.OTHER_STUDENT, EnrollmentStatus.REJECTED, List.of()),
                Arguments.arguments(TestActor.OTHER_STUDENT, EnrollmentStatus.CANCELLED, List.of()),
                Arguments.arguments(TestActor.OTHER_STUDENT, EnrollmentStatus.DROPPED, List.of())
            );
        }

        @ParameterizedTest(name = "{0} on {1} -> {2}")
        @MethodSource("cases")
        void shouldCorrectlyDetermineAllowedActions(TestActor actor, EnrollmentStatus from, List<EnrollmentAction> expectedAllowedActions) {
            var target = seedTarget(from, SEATS_AVAILABLE, SemesterStatus.PLANNED);
            var other = seedOther();

            var user = getUserFor(actor, target.enrollment(), other.enrollment());

            assertEquals(expectedAllowedActions, EnrollmentActionPolicy.allowedActions(target.enrollment(), user));
        }
    }

    @Nested
    class FullOffering {
        // Only APPROVE needs a free seat; every other action must stay available when the offering is full
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.PENDING, List.of(EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.PENDING, List.of(EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.PENDING, List.of(EnrollmentAction.CANCEL)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.ENROLLED, List.of(EnrollmentAction.DROP))
            );
        }

        @ParameterizedTest(name = "{0} on {1} -> {2}")
        @MethodSource("cases")
        void shouldOnlyBlockApprovalWhenOfferingFull(TestActor actor, EnrollmentStatus from, List<EnrollmentAction> expectedAllowedActions) {
            var target = seedTarget(from, OFFERING_FULL, SemesterStatus.PLANNED);
            var other = seedOther();

            var user = getUserFor(actor, target.enrollment(), other.enrollment());

            assertEquals(expectedAllowedActions, EnrollmentActionPolicy.allowedActions(target.enrollment(), user));
        }
    }

    @Nested
    class ClosedSemester {
        // A closed semester blocks new approvals only; pending requests can still be rejected or cancelled
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.PENDING, SemesterStatus.COMPLETED, List.of(EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.ADMIN, EnrollmentStatus.PENDING, SemesterStatus.CANCELLED, List.of(EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.PENDING, SemesterStatus.COMPLETED, List.of(EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentStatus.PENDING, SemesterStatus.CANCELLED, List.of(EnrollmentAction.REJECT)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.PENDING, SemesterStatus.COMPLETED, List.of(EnrollmentAction.CANCEL)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentStatus.ENROLLED, SemesterStatus.COMPLETED, List.of(EnrollmentAction.DROP))
            );
        }

        @ParameterizedTest(name = "{0} on {1} in {2} semester -> {3}")
        @MethodSource("cases")
        void shouldOnlyBlockApprovalWhenSemesterClosed(TestActor actor, EnrollmentStatus from, SemesterStatus semesterStatus, List<EnrollmentAction> expectedAllowedActions) {
            var target = seedTarget(from, SEATS_AVAILABLE, semesterStatus);
            var other = seedOther();

            var user = getUserFor(actor, target.enrollment(), other.enrollment());

            assertEquals(expectedAllowedActions, EnrollmentActionPolicy.allowedActions(target.enrollment(), user));
        }
    }

    @Nested
    class DenialPrecedence {
        // Each row breaks more than one rule; the denial returned shows which check runs first.
        // Actor runs before everything else so outsiders never learn anything about the enrollment's state.
        static Stream<Arguments> cases() {
            return Stream.of(
                Arguments.arguments(TestActor.ADMIN, EnrollmentAction.APPROVE, EnrollmentStatus.PENDING, SEATS_AVAILABLE, SemesterStatus.PLANNED, Optional.empty()),

                Arguments.arguments(TestActor.OTHER_STUDENT, EnrollmentAction.APPROVE, EnrollmentStatus.ENROLLED, OFFERING_FULL, SemesterStatus.COMPLETED, Optional.of(EnrollmentDenial.NOT_PERMITTED)),
                Arguments.arguments(TestActor.OTHER_INSTRUCTOR, EnrollmentAction.APPROVE, EnrollmentStatus.PENDING, OFFERING_FULL, SemesterStatus.PLANNED, Optional.of(EnrollmentDenial.NOT_PERMITTED)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentAction.APPROVE, EnrollmentStatus.PENDING, OFFERING_FULL, SemesterStatus.PLANNED, Optional.of(EnrollmentDenial.NOT_PERMITTED)),
                Arguments.arguments(TestActor.OFFERING_INSTRUCTOR, EnrollmentAction.DROP, EnrollmentStatus.ENROLLED, SEATS_AVAILABLE, SemesterStatus.PLANNED, Optional.of(EnrollmentDenial.NOT_PERMITTED)),

                Arguments.arguments(TestActor.ADMIN, EnrollmentAction.APPROVE, EnrollmentStatus.ENROLLED, OFFERING_FULL, SemesterStatus.COMPLETED, Optional.of(EnrollmentDenial.INVALID_TRANSITION)),
                Arguments.arguments(TestActor.OWNING_STUDENT, EnrollmentAction.DROP, EnrollmentStatus.PENDING, SEATS_AVAILABLE, SemesterStatus.PLANNED, Optional.of(EnrollmentDenial.INVALID_TRANSITION)),

                Arguments.arguments(TestActor.ADMIN, EnrollmentAction.APPROVE, EnrollmentStatus.PENDING, OFFERING_FULL, SemesterStatus.COMPLETED, Optional.of(EnrollmentDenial.SEMESTER_CLOSED)),
                Arguments.arguments(TestActor.ADMIN, EnrollmentAction.APPROVE, EnrollmentStatus.PENDING, OFFERING_FULL, SemesterStatus.PLANNED, Optional.of(EnrollmentDenial.OFFERING_FULL))
            );
        }

        @ParameterizedTest(name = "{0} {1} on {2} (enrolled {3}, {4} semester) -> {5}")
        @MethodSource("cases")
        void shouldReturnFirstFailingRule(
            TestActor actor,
            EnrollmentAction action,
            EnrollmentStatus from,
            int enrolled,
            SemesterStatus semesterStatus,
            Optional<EnrollmentDenial> expectedDenial
        ) {
            var target = seedTarget(from, enrolled, semesterStatus);
            var other = seedOther();

            var user = getUserFor(actor, target.enrollment(), other.enrollment());

            assertEquals(expectedDenial, EnrollmentActionPolicy.validate(target.enrollment(), action, user));
        }
    }
}
