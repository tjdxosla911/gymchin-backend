package com.gymchin.api.matching.controller;

import com.gymchin.api.common.dto.ApiResponse;
import com.gymchin.api.common.dto.PageResponse;
import com.gymchin.api.common.security.CurrentUserIdResolver;
import com.gymchin.api.matching.service.GymmateService;
import com.gymchin.api.user.dto.GymmateSearchCriteria;
import com.gymchin.api.user.dto.UserSummaryDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gymmates")
public class GymmateController {

    private final GymmateService gymmateService;
    private final CurrentUserIdResolver currentUserIdResolver;

    public GymmateController(GymmateService gymmateService, CurrentUserIdResolver currentUserIdResolver) {
        this.gymmateService = gymmateService;
        this.currentUserIdResolver = currentUserIdResolver;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserSummaryDto>> getGymmates(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String district,
        @RequestParam(required = false) String fitnessLevel,
        @RequestParam(required = false) String goal,
        @RequestParam(required = false) String day,
        @RequestParam(required = false) String timeSlot,
        @RequestParam(required = false) String coachOption
    ) {
        Long requesterUserId = currentUserIdResolver.resolve(authentication);
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        GymmateSearchCriteria criteria = new GymmateSearchCriteria(
            city,
            district,
            fitnessLevel,
            goal,
            day,
            timeSlot,
            coachOption
        );
        PageResponse<UserSummaryDto> response = gymmateService.searchGymmates(requesterUserId, criteria, pageable);
        return ApiResponse.success(response);
    }
}
