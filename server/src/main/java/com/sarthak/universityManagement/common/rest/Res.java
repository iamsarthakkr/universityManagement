package com.sarthak.universityManagement.common.rest;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;

public class Res {
    public static ResponseEntity<ApiResponse<Void>> success() { return successCode(SuccessCode.OK); }
    public static <T> ResponseEntity<ApiResponse<T>> success(T body) { return success(SuccessCode.OK.message(), body); }
    public static ResponseEntity<ApiResponse<Void>> successCode(SuccessCode successCode) { return success(successCode, successCode.message(), null); }
    public static ResponseEntity<ApiResponse<Void>> successMessage(String message) { return success(SuccessCode.OK, message, null); }
    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessCode successCode, T body) { return success(successCode, null, body); }
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T body) { return success(SuccessCode.OK, message, body); }
    public static <T> ResponseEntity<ApiResponse<T>> success(@NonNull SuccessCode successCode, String message, T body) {
        String finalMessage = message == null ? successCode.message() : message;
        return ResponseEntity.status(successCode.status()).body(ApiResponse.success(finalMessage, body));
    }
    
    public static ResponseEntity<ApiErrorResponse<Void>> error() { return errorCode(ErrorCode.INTERNAL_SERVER_ERROR); }
    public static <T> ResponseEntity<ApiErrorResponse<T>> error(T errors) { return error(ErrorCode.INTERNAL_SERVER_ERROR.message(), errors); }
    public static ResponseEntity<ApiErrorResponse<Void>> errorCode(ErrorCode errorCode) { return error(errorCode, errorCode.message(), null); }
    public static ResponseEntity<ApiErrorResponse<Void>> errorMessage(String message) { return error(ErrorCode.INTERNAL_SERVER_ERROR,  message, null); }
    public static ResponseEntity<ApiErrorResponse<Void>> error(ErrorCode errorCode, String message) { return error(errorCode, message, null); }
    public static <T> ResponseEntity<ApiErrorResponse<T>> error(String message, T errors) { return error(ErrorCode.INTERNAL_SERVER_ERROR, message, errors); }
    public static <T> ResponseEntity<ApiErrorResponse<T>> error(@NonNull ErrorCode errorCode, String message, T errors) {
        String finalMessage = message == null ? errorCode.message() : message;
        return ResponseEntity.status(errorCode.status()).body(ApiErrorResponse.error(finalMessage, errors));
    }
    
}
