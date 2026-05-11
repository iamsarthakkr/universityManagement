package com.sarthak.universityEnrollmentManagement.service;

import com.sarthak.universityEnrollmentManagement.repo.InstructorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstructorService {
    private final InstructorRepo instructorRepo;
    
}
