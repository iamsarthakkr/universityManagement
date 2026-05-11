package com.sarthak.universityEnrollmentManagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sarthak.universityEnrollmentManagement.repo.StudentRepo;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepo studentRepo;

}
