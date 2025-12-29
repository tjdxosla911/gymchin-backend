package com.gymchin.api.common.error;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final Object details;

    public AppException(ErrorCode errorCode, String message, HttpStatus status, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.details = details;
    }

    public static AppException of(ErrorCode errorCode, String message, HttpStatus status) {
        return new AppException(errorCode, message, status, null);
    }

    public static AppException of(ErrorCode errorCode, String message, HttpStatus status, Object details) {
        return new AppException(errorCode, message, status, details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
