package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.request.InstructorRegistrationRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.InstructorRegistrationResponse;
import com.sarthak.universityEnrollmentManagement.entity.InstructorRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import com.sarthak.universityEnrollmentManagement.mapper.InstructorRegistrationMapper;
import com.sarthak.universityEnrollmentManagement.repo.InstructorRegistrationRepo;
import com.sarthak.universityEnrollmentManagement.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorRegistrationService {
    private final InstructorRegistrationRepo instructorRegistrationRepo;
    private final RegistrationValidator registrationValidator;
    
    @Transactional
    public void createRegistration(InstructorRegistrationRequest instructorRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(instructorRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(instructorRegistrationRequest.email());
        
        InstructorRegistrationEntity toSave = InstructorRegistrationMapper.toEntity(instructorRegistrationRequest);
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        instructorRegistrationRepo.save(toSave);
    }
    
    @Transactional(readOnly = true)
    public List<InstructorRegistrationResponse> getPendingRequests() {
        return instructorRegistrationRepo
            .findByRegistrationStatus(RegistrationStatus.PENDING)
            .stream()
            .map(InstructorRegistrationMapper::toResponse)
            .toList();
    }
    
}
