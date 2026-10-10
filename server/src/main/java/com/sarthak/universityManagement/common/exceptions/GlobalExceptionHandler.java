package com.sarthak.universityManagement.common.exceptions;

import com.sarthak.universityManagement.common.rest.ApiErrorResponse;
import com.sarthak.universityManagement.common.rest.ErrorCode;
import com.sarthak.universityManagement.common.rest.Res;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    /* ---------------------------------------------- domain exceptions ---------------------------------------------- */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        LOG.warn("Not found: {}", ex.getMessage());
        return Res.error(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleConflict(ConflictException ex) {
        LOG.warn("Conflict: {}", ex.getMessage());
        return Res.error(ErrorCode.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleBadRequest(BadRequestException ex) {
        LOG.warn("Bad request: {}", ex.getMessage());
        return Res.error(ErrorCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleForbidden(ForbiddenException ex) {
        LOG.warn("Forbidden: {}", ex.getMessage());
        return Res.error(ErrorCode.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        LOG.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return Res.error(ErrorCode.CONFLICT, "This request conflicts with existing data");
    }

    /* ----------------------------------------------------- security ------------------------------------------------ */

    @ExceptionHandler({
        BadCredentialsException.class,
        AccountStatusException.class
    })
    public ResponseEntity<ApiErrorResponse<Void>> handleLoginFailure(AuthenticationException ex) {
        LOG.warn("Login failed: {}", ex.getMessage());
        return Res.error(ErrorCode.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleInternalAuthenticationFailure(InternalAuthenticationServiceException ex) {
        LOG.error("Authentication failed internally", ex);
        return Res.error(ErrorCode.INTERNAL_SERVER_ERROR, "Login is temporarily unavailable. Please try again later");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleAuthentication(AuthenticationException ex) {
        LOG.warn("Authentication failed: {}", ex.getMessage());
        return Res.error(ErrorCode.UNAUTHORIZED, "Authentication required");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        LOG.warn("Access denied: {}", ex.getMessage());
        return Res.error(ErrorCode.FORBIDDEN, "You do not have permission to perform this action");
    }

    /* --------------------------------------------------- request shape --------------------------------------------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        // LinkedHashMap keeps the fields in the order the client sent them
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        LOG.warn("Validation failed: {}", errors);
        return Res.error(ErrorCode.VALIDATION_FAILED, "Some fields are invalid", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var message = "Invalid value '" + ex.getValue() + "' for '" + ex.getName() + "'";
        var requiredType = ex.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            message += ". Allowed values: " + allowedValues(requiredType);
        }

        LOG.warn("Type mismatch: {}", message);
        return Res.error(ErrorCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        LOG.warn("Missing parameter: {}", ex.getParameterName());
        return Res.error(ErrorCode.BAD_REQUEST, "Missing required parameter '" + ex.getParameterName() + "'");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        LOG.warn("Unreadable request body: {}", ex.getMostSpecificCause().getMessage());
        return Res.error(ErrorCode.BAD_REQUEST, "Request body is missing or malformed");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        LOG.warn("Unsupported media type: {}", ex.getContentType());
        return Res.error(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Content type '" + ex.getContentType() + "' is not supported; use 'application/json'");
    }

    /* ------------------------------------------------------ routing ------------------------------------------------ */

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleNoEndpoint(NoResourceFoundException ex, HttpServletRequest request) {
        var endpoint = request.getMethod() + " " + request.getRequestURI();
        LOG.warn("No endpoint: {}", endpoint);
        return Res.error(ErrorCode.RESOURCE_NOT_FOUND, "No endpoint " + endpoint);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        var supported = ex.getSupportedMethods() == null ? "" : String.join(", ", ex.getSupportedMethods());
        LOG.warn("Method not allowed: {} {}", ex.getMethod(), request.getRequestURI());
        return Res.error(
            ErrorCode.METHOD_NOT_ALLOWED,
            "Method " + ex.getMethod() + " is not supported for " + request.getRequestURI() + ". Supported: " + supported
        );
    }

    /* ----------------------------------------------------- fallback ------------------------------------------------ */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse<Void>> handleUnexpected(Exception ex) {
        LOG.error("Unexpected error", ex);
        return Res.error(ErrorCode.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later");
    }

    private static String allowedValues(Class<?> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    }
}
