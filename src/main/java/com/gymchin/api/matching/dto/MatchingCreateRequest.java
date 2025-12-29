package com.gymchin.api.matching.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MatchingCreateRequest {
    @NotNull
    private Long targetUserId;

    @Size(max = 500)
    private String message;

    public MatchingCreateRequest() {
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
