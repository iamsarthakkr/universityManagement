package com.sarthak.universityManagement.common.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ApiResponse<T> {
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("body")
    private T body;
    
    @JsonProperty("isSuccess")
    private boolean isSuccess;
    
    @JsonProperty("timestamp")
    private final Instant timestamp = Instant.now();
    
    private static final String SUCCESS_MESSAGE = "Success";
    
    public static ApiResponse<Void> success() { return success(SUCCESS_MESSAGE, null); }
    public static <T> ApiResponse<T> success(T body) { return success(SUCCESS_MESSAGE, body); }
    public static ApiResponse<Void> successMessage(String message) { return success(message, null); }
    public static <T> ApiResponse<T> success(String message, T body) {
        return new ApiResponse<>(message, body, true);
    }
    
}
