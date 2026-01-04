package com.gymchin.api.user.controller;

import com.gymchin.api.common.dto.ApiResponse;
import com.gymchin.api.user.dto.AuthLoginRequest;
import com.gymchin.api.user.dto.AuthLogoutRequest;
import com.gymchin.api.user.dto.AuthRefreshRequest;
import com.gymchin.api.user.dto.AuthSignupRequest;
import com.gymchin.api.user.dto.AuthTokenResponse;
import com.gymchin.api.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> signup(@Valid @RequestBody AuthSignupRequest request) {
        AuthTokenResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody AuthRefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody AuthLogoutRequest request) {
        authService.logout(request);
        return ApiResponse.success(null);
    }
}
