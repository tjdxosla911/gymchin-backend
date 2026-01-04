package com.gymchin.api.user.service;

import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import com.gymchin.api.common.security.JwtService;
import com.gymchin.api.user.dto.AuthLoginRequest;
import com.gymchin.api.user.dto.AuthLogoutRequest;
import com.gymchin.api.user.dto.AuthRefreshRequest;
import com.gymchin.api.user.dto.AuthSignupRequest;
import com.gymchin.api.user.dto.AuthTokenResponse;
import com.gymchin.api.user.entity.RefreshToken;
import com.gymchin.api.user.entity.User;
import com.gymchin.api.user.repository.RefreshTokenRepository;
import com.gymchin.api.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    public AuthTokenResponse signup(AuthSignupRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getPassword() != null && !user.getPassword().equals(request.getPassword())) {
                throw AppException.of(ErrorCode.CONFLICT, "Email already exists", HttpStatus.CONFLICT);
            }
            return issueTokens(user);
        }

        User user = new User(request.getEmail(), request.getNickname());
        user.setPassword(request.getPassword());
        User saved = userRepository.save(user);
        return issueTokens(saved);
    }

    public AuthTokenResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> AppException.of(
                ErrorCode.AUTH_INVALID_CREDENTIALS,
                "Invalid credentials",
                HttpStatus.UNAUTHORIZED
            ));
        if (user.getPassword() == null || !user.getPassword().equals(request.getPassword())) {
            throw AppException.of(ErrorCode.AUTH_INVALID_CREDENTIALS, "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return issueTokens(user);
    }

    public AuthTokenResponse refresh(AuthRefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Unauthorized", HttpStatus.UNAUTHORIZED));

        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.deleteByToken(stored.getToken());
            throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        return rotateTokens(stored.getUser());
    }

    public void logout(AuthLogoutRequest request) {
        Optional<RefreshToken> stored = refreshTokenRepository.findByToken(request.getRefreshToken());
        if (stored.isEmpty()) {
            throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
    }

    private AuthTokenResponse issueTokens(User user) {
        return upsertTokens(user);
    }

    private AuthTokenResponse rotateTokens(User user) {
        return upsertTokens(user);
    }

    private AuthTokenResponse upsertTokens(User user) {
        String accessToken = jwtService.createAccessToken(user.getId());
        String refreshToken = jwtService.createRefreshToken(user.getId());
        OffsetDateTime expiresAt = jwtService.getRefreshExpiry();
        RefreshToken stored = refreshTokenRepository.findByUserId(user.getId())
            .map(existing -> {
                existing.setToken(refreshToken);
                existing.setExpiresAt(expiresAt);
                return existing;
            })
            .orElseGet(() -> new RefreshToken(user, refreshToken, expiresAt));
        refreshTokenRepository.save(stored);
        return new AuthTokenResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTtlSeconds());
    }
}
