package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.dto.request.StudentRegistrationRequest;
import com.sarthak.universityEnrollmentManagement.dto.response.StudentRegistrationResponse;
import com.sarthak.universityEnrollmentManagement.entity.StudentRegistrationEntity;
import com.sarthak.universityEnrollmentManagement.entity.types.RegistrationStatus;
import com.sarthak.universityEnrollmentManagement.mapper.StudentRegistrationMapper;
import com.sarthak.universityEnrollmentManagement.repo.StudentRegistrationRepo;
import com.sarthak.universityEnrollmentManagement.validators.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentRegistrationService {
    private final StudentRegistrationRepo studentRegistrationRepo;
    private final RegistrationValidator registrationValidator;
    
    @Transactional
    public void createRegistration(StudentRegistrationRequest studentRegistrationRequest) {
        registrationValidator.validateUserNameAvailable(studentRegistrationRequest.username());
        registrationValidator.validateEmailAvailable(studentRegistrationRequest.email());
        
        StudentRegistrationEntity toSave = StudentRegistrationMapper.toEntity(studentRegistrationRequest);
        toSave.setRegistrationStatus(RegistrationStatus.PENDING);
        studentRegistrationRepo.save(toSave);
    }
    
    @Transactional(readOnly = true)
    public List<StudentRegistrationResponse> getPendingRequests() {
        return studentRegistrationRepo
            .findByRegistrationStatus(RegistrationStatus.PENDING)
            .stream()
            .map(StudentRegistrationMapper::toResponse)
            .toList();
    }
    
}
