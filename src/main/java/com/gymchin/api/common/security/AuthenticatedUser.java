package com.gymchin.api.common.security;

public class AuthenticatedUser {
    private final Long userId;

    public AuthenticatedUser(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
