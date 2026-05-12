package com.sarthak.universityManagement.common.rest;

import org.springframework.http.HttpStatus;

public enum SuccessCode {
    OK(HttpStatus.OK, "Success"),
    CREATED(HttpStatus.CREATED, "Created successfully");
    
    private final HttpStatus status;
    private final String message;
    
    SuccessCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public HttpStatus status() {
        return status;
    }
    
    public String message() {
        return message;
    }
}
