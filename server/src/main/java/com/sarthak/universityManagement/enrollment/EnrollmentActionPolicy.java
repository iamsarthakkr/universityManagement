package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.enrollment.types.EnrollmentDenial;
import com.sarthak.universityManagement.security.UserPrincipal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EnrollmentActionPolicy {
    enum EnrollmentActor { ADMIN, OFFERING_INSTRUCTOR, OWNING_STUDENT, NONE }

    static final Map<EnrollmentActor, Set<EnrollmentAction>> PERMITTED_ACTIONS = Map.of(
        EnrollmentActor.ADMIN, Set.of(EnrollmentAction.APPROVE, EnrollmentAction.REJECT),
        EnrollmentActor.OFFERING_INSTRUCTOR, Set.of(EnrollmentAction.APPROVE, EnrollmentAction.REJECT),
        EnrollmentActor.OWNING_STUDENT, Set.of(EnrollmentAction.CANCEL, EnrollmentAction.DROP),
        EnrollmentActor.NONE, Set.of()
    );

    static EnrollmentActor getActor(
        EnrollmentEntity enrollment,
        UserPrincipal currentUser
    ) {
        return switch (currentUser.getRole()) {
            case ADMIN -> EnrollmentActor.ADMIN;
            case INSTRUCTOR -> enrollment.isTaughtBy(currentUser.getUserId()) ? EnrollmentActor.OFFERING_INSTRUCTOR : EnrollmentActor.NONE;
            case STUDENT -> enrollment.belongsToUser(currentUser.getUserId()) ? EnrollmentActor.OWNING_STUDENT : EnrollmentActor.NONE;
        };
    }

    static Optional<EnrollmentDenial> businessRule(
        EnrollmentEntity enrollment,
        EnrollmentAction action
    ) {
        if(!enrollment.canTransitionTo(action.getTargetStatus())) {
            return Optional.of(EnrollmentDenial.INVALID_TRANSITION);
        }

        var offering = enrollment.getCourseOffering();
        return switch (action) {
            case APPROVE ->
                !offering.getSemester().allowsEnrollment() ? Optional.of(EnrollmentDenial.SEMESTER_CLOSED) :
                !offering.canEnroll() ? Optional.of(EnrollmentDenial.OFFERING_FULL) :
                Optional.empty();
            case REJECT,
                 CANCEL,
                 DROP -> Optional.empty();
        };
    }

    public static Optional<EnrollmentDenial> validate(
        EnrollmentEntity enrollment,
        EnrollmentAction action,
        UserPrincipal user
    ) {
        if(!PERMITTED_ACTIONS.get(getActor(enrollment, user)).contains(action)) {
            return Optional.of(EnrollmentDenial.NOT_PERMITTED);
        }
        return businessRule(enrollment, action);
    }


    public static List<EnrollmentAction> allowedActions(
        EnrollmentEntity enrollment,
        UserPrincipal user
    ) {
        return Arrays.stream(EnrollmentAction.values())
            .filter(action -> validate(enrollment, action, user).isEmpty())
            .toList();
    }

}
