package com.agile.board.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private final Instant timestamp = Instant.now();
    private final int status;           // HTTP status code (e.g., 400)
    private final String error;         // HTTP reason (e.g., "Bad Request")
    private final String code;          // App-specific code (e.g., "VALIDATION_ERROR")
    private final String message;       // Human readable message
    private final String path;          // Request path
    private final Map<String, String> details; // Optional key/value details

    public ApiError(int status, String error, String code, String message, String path, Map<String, String> details) {
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public Map<String, String> getDetails() { return details; }
}