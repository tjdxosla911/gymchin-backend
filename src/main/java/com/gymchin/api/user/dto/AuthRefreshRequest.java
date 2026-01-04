package com.gymchin.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRefreshRequest {
    @NotBlank
    private String refreshToken;

    public AuthRefreshRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
