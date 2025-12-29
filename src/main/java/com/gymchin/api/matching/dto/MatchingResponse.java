package com.gymchin.api.matching.dto;

import com.gymchin.api.matching.entity.MatchingStatus;
import java.time.OffsetDateTime;

public class MatchingResponse {
    private final Long matchingId;
    private final Long requesterUserId;
    private final Long targetUserId;
    private final MatchingStatus status;
    private final String message;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public MatchingResponse(Long matchingId, Long requesterUserId, Long targetUserId, MatchingStatus status,
                            String message, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.matchingId = matchingId;
        this.requesterUserId = requesterUserId;
        this.targetUserId = targetUserId;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getMatchingId() {
        return matchingId;
    }

    public Long getRequesterUserId() {
        return requesterUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public MatchingStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
