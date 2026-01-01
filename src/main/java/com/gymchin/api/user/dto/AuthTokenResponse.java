package com.gymchin.api.user.dto;

public class AuthTokenResponse {
    private final String accessToken;

    public AuthTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
