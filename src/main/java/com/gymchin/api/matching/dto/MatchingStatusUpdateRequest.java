package com.gymchin.api.matching.dto;

import com.gymchin.api.matching.entity.MatchingStatus;
import jakarta.validation.constraints.NotNull;

public class MatchingStatusUpdateRequest {
    @NotNull
    private MatchingStatus status;

    public MatchingStatusUpdateRequest() {
    }

    public MatchingStatus getStatus() {
        return status;
    }

    public void setStatus(MatchingStatus status) {
        this.status = status;
    }
}
