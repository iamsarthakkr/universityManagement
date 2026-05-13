package com.sarthak.universityManagement.common.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ApiErrorResponse<T> {
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("errors")
    private T errors;
    
    @JsonProperty("isSuccess")
    private boolean isSuccess;
    
    @JsonProperty("timestamp")
    private final Instant timestamp = Instant.now();
    
    private static final String ERROR_MESSAGE = "Error";
    
    public static ApiErrorResponse<Void> error() { return error(ERROR_MESSAGE, null); }
    public static <T> ApiErrorResponse<T> error(T errors) { return error(ERROR_MESSAGE, errors); }
    public static ApiErrorResponse<Void> errorMessage(String message) { return error(message, null); }
    public static <T> ApiErrorResponse<T> error(String message, T errors) {
        return new ApiErrorResponse<>(message, errors, false);
    }
    
}
