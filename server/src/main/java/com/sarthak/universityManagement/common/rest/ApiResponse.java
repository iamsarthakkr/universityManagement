package com.sarthak.universityManagement.common.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    @JsonProperty("message") String message,
    @JsonProperty("data") T body,
    @JsonProperty("isSuccess") boolean isSuccess
) {
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String ERROR_MESSAGE = "Error";
    
    public static ApiResponse<Void> success() {
        return success(SUCCESS_MESSAGE, null);
    }
    
    public static <T> ApiResponse<T> success(T body) {
        return success(SUCCESS_MESSAGE, body);
    }
    
    public static <T> ApiResponse<T> success(String message, T body) {
        return new ApiResponse<>(message, body, true);
    }
    
    public static ApiResponse<Void> error() {
        return error(ERROR_MESSAGE, null);
    }
    
    public static <T> ApiResponse<T> error(T body) {
        return error("Success", body);
    }
    
    public static <T> ApiResponse<T> error(String message, T body) {
        return new ApiResponse<>(message, body, true);
    }
    
}
