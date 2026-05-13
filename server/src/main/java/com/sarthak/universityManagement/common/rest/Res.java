package com.sarthak.universityManagement.common.rest;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;

public class Res {
    public static ResponseEntity<ApiResponse<Void>> success() { return successCode(SuccessCode.OK); }
    public static ResponseEntity<ApiResponse<Void>> successCode(SuccessCode successCode) { return success(successCode, successCode.message(), null); }
    public static <T> ResponseEntity<ApiResponse<T>> success(T body) { return success(SuccessCode.OK.message(), body); }
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T body) { return success(SuccessCode.OK, message, body); }
    public static <T> ResponseEntity<ApiResponse<T>> success(@NonNull SuccessCode successCode, String message, T body) {
        String finalMessage = message == null ? successCode.message() : message;
        return ResponseEntity.status(successCode.status()).body(ApiResponse.success(finalMessage, body));
    }
    
    public static ResponseEntity<ApiResponse<Void>> error() { return errorCode(ErrorCode.INTERNAL_SERVER_ERROR); }
    public static ResponseEntity<ApiResponse<Void>> errorCode(ErrorCode successCode) { return error(successCode, successCode.message(), null); }
    public static <T> ResponseEntity<ApiResponse<T>> error(T body) { return error(ErrorCode.INTERNAL_SERVER_ERROR.message(), body); }
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, T body) { return error(ErrorCode.INTERNAL_SERVER_ERROR, message, body); }
    public static <T> ResponseEntity<ApiResponse<T>> error(@NonNull ErrorCode error, String message, T body) {
        String finalMessage = message == null ? error.message() : message;
        return ResponseEntity.status(error.status()).body(ApiResponse.error(finalMessage, body));
    }
}
