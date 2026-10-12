package com.sarthak.universityManagement.registration.validators;

import com.sarthak.universityManagement.common.exceptions.ConflictException;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final UserRepo userRepo;

    // A rejected request frees its username and email again; an account or a request still under review does not.
    // One message for every case, so this public endpoint does not reveal which accounts or requests exist
    public void validateCredentialsAvailable(String username, String email) {
        var taken = userRepo.existsByUsername(username)
            || userRepo.existsByEmail(email)
            || studentRegistrationRepo.existsByUsernameAndRegistrationStatus(username, RegistrationStatus.PENDING)
            || studentRegistrationRepo.existsByEmailAndRegistrationStatus(email, RegistrationStatus.PENDING)
            || instructorRegistrationRepo.existsByUsernameAndRegistrationStatus(username, RegistrationStatus.PENDING)
            || instructorRegistrationRepo.existsByEmailAndRegistrationStatus(email, RegistrationStatus.PENDING);

        if(taken) {
            throw new ConflictException("Username or email is not available");
        }
    }
}
