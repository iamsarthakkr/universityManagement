package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import com.sarthak.universityManagement.security.UserPrincipal;

import java.util.Arrays;
import java.util.List;

public class EnrollmentActionPolicy {
    public static List<EnrollmentAction> allowedActions(
        EnrollmentEntity enrollment,
        UserPrincipal currentUser
    ) {
        return Arrays.stream(EnrollmentAction.values())
            .filter(action -> canPerform(enrollment, currentUser, action))
            .toList();
    }

    public static boolean canPerform(
        EnrollmentEntity enrollment,
        UserPrincipal currentUser,
        EnrollmentAction action
    ) {
        var offering = enrollment.getCourseOffering();
        var semester = offering.getSemester();
        var offeringInstructor = offering.getInstructor();
        if(!semester.allowsEnrollment()) {
            return false;
        }

        if(action.equals(EnrollmentAction.APPROVE) && !offering.canEnroll()) {
            return false;
        }

        if(currentUser.getRole().equals(Role.INSTRUCTOR) && offeringInstructor.getUser().getId() != currentUser.getUserId()) {
            return false;
        }

        return enrollment.canTransitionTo(action.getTargetStatus());
    }
}
