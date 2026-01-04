package com.gymchin.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthLogoutRequest {
    @NotBlank
    private String refreshToken;

    public AuthLogoutRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
