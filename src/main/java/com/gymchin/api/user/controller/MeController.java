package com.gymchin.api.user.controller;

import com.gymchin.api.common.dto.ApiResponse;
import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import com.gymchin.api.common.security.CurrentUserIdResolver;
import com.gymchin.api.user.dto.UserSummaryDto;
import com.gymchin.api.user.entity.User;
import com.gymchin.api.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserRepository userRepository;
    private final CurrentUserIdResolver currentUserIdResolver;

    public MeController(UserRepository userRepository, CurrentUserIdResolver currentUserIdResolver) {
        this.userRepository = userRepository;
        this.currentUserIdResolver = currentUserIdResolver;
    }

    @GetMapping
    public ApiResponse<UserSummaryDto> getMe(Authentication authentication) {
        Long userId = currentUserIdResolver.resolve(authentication);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> AppException.of(ErrorCode.RESOURCE_NOT_FOUND, "User not found", HttpStatus.NOT_FOUND));
        UserSummaryDto response = new UserSummaryDto(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getGender(),
            user.getAge(),
            user.getCity(),
            user.getDistrict(),
            user.getFitnessLevel(),
            user.getGoals(),
            user.getPreferredDays(),
            user.getPreferredTimeSlots(),
            user.getCoachOption(),
            user.getGymName(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
        return ApiResponse.success(response);
    }
}
