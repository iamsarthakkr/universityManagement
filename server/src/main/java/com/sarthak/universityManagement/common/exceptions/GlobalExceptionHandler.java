package com.sarthak.universityManagement.common.exceptions;

import com.sarthak.universityManagement.common.rest.ApiErrorResponse;
import com.sarthak.universityManagement.common.rest.ErrorCode;
import com.sarthak.universityManagement.common.rest.Res;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        LOG.error(ex.getMessage(), ex);
        return Res.error(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleConflict(ConflictException ex) {
        LOG.error(ex.getMessage(), ex);
        return Res.error(ErrorCode.CONFLICT, ex.getMessage());
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleBadRequest(BadRequestException ex) {
        LOG.error(ex.getMessage(), ex);
        return Res.error(ErrorCode.BAD_REQUEST, ex.getMessage());
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        LOG.error(ex.getMessage(), ex);
        return Res.error(ErrorCode.CONFLICT, "Request violates a database constraint");
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleGenericException(Exception ex) {
        LOG.error(ex.getMessage(), ex);
        return Res.errorMessage("Something went wrong");
    }
}
