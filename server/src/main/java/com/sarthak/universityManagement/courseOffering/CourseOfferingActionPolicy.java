package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.courseOffering.types.CourseOfferingAction;
import com.sarthak.universityManagement.courseOffering.types.EnrollDenial;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CourseOfferingActionPolicy {
    public static Optional<EnrollDenial> validateEnroll(
        CourseOfferingEntity offering, Role role, LocalDate today
    ) {
        if(role != Role.STUDENT) return Optional.of(EnrollDenial.NOT_A_STUDENT);
        if(!offering.getSemester().isRegistrationOpen(today)) return Optional.of(EnrollDenial.REGISTRATION_CLOSED);
        if(!offering.canEnroll()) return Optional.of(EnrollDenial.OFFERING_FULL);
        return Optional.empty();
    }

    public static List<CourseOfferingAction> allowedActions(
        CourseOfferingEntity offering, Role role, LocalDate today
    ) {
        return validateEnroll(offering, role, today).isEmpty()
            ? List.of(CourseOfferingAction.ENROLL)
            : List.of();
    }
}
