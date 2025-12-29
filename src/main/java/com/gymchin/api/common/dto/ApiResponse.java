package com.gymchin.api.common.dto;

import com.gymchin.api.common.error.AppError;
import java.time.OffsetDateTime;

public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final AppError error;
    private final OffsetDateTime timestamp;

    private ApiResponse(boolean success, T data, AppError error, OffsetDateTime timestamp) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> failure(AppError error) {
        return new ApiResponse<>(false, null, error, OffsetDateTime.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public AppError getError() {
        return error;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}
