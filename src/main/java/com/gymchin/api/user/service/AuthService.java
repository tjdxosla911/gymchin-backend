package com.gymchin.api.user.service;

import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import com.gymchin.api.user.dto.AuthLoginRequest;
import com.gymchin.api.user.dto.AuthSignupRequest;
import com.gymchin.api.user.dto.AuthTokenResponse;
import com.gymchin.api.user.entity.User;
import com.gymchin.api.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthTokenResponse signup(AuthSignupRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            return new AuthTokenResponse(existing.get().getId().toString());
        }

        User user = new User(request.getEmail(), request.getNickname());
        User saved = userRepository.save(user);
        return new AuthTokenResponse(saved.getId().toString());
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> AppException.of(
                ErrorCode.AUTH_INVALID_CREDENTIALS,
                "Invalid credentials",
                HttpStatus.UNAUTHORIZED
            ));
        return new AuthTokenResponse(user.getId().toString());
    }
}
