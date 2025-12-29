package com.gymchin.api.matching.controller;

import com.gymchin.api.common.dto.ApiResponse;
import com.gymchin.api.common.dto.PageResponse;
import com.gymchin.api.common.security.CurrentUserIdResolver;
import com.gymchin.api.matching.dto.MatchingCreateRequest;
import com.gymchin.api.matching.dto.MatchingResponse;
import com.gymchin.api.matching.dto.MatchingStatusUpdateRequest;
import com.gymchin.api.matching.entity.MatchingStatus;
import com.gymchin.api.matching.service.MatchingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matchings")
public class MatchingController {

    private final MatchingService matchingService;
    private final CurrentUserIdResolver currentUserIdResolver;

    public MatchingController(MatchingService matchingService, CurrentUserIdResolver currentUserIdResolver) {
        this.matchingService = matchingService;
        this.currentUserIdResolver = currentUserIdResolver;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MatchingResponse>> createMatching(
        Authentication authentication,
        @Valid @RequestBody MatchingCreateRequest request
    ) {
        Long requesterUserId = currentUserIdResolver.resolve(authentication);
        MatchingResponse response = matchingService.createMatching(requesterUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/sent")
    public ApiResponse<PageResponse<MatchingResponse>> getSent(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) MatchingStatus status
    ) {
        Long requesterUserId = currentUserIdResolver.resolve(authentication);
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<MatchingResponse> result = matchingService.getSentMatchings(requesterUserId, status, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/received")
    public ApiResponse<PageResponse<MatchingResponse>> getReceived(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) MatchingStatus status
    ) {
        Long targetUserId = currentUserIdResolver.resolve(authentication);
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<MatchingResponse> result = matchingService.getReceivedMatchings(targetUserId, status, pageable);
        return ApiResponse.success(toPageResponse(result));
    }

    @GetMapping("/{matchingId}")
    public ApiResponse<MatchingResponse> getMatching(
        Authentication authentication,
        @PathVariable Long matchingId
    ) {
        Long userId = currentUserIdResolver.resolve(authentication);
        return ApiResponse.success(matchingService.getMatching(userId, matchingId));
    }

    @PatchMapping("/{matchingId}/status")
    public ApiResponse<MatchingResponse> updateStatus(
        Authentication authentication,
        @PathVariable Long matchingId,
        @Valid @RequestBody MatchingStatusUpdateRequest request
    ) {
        Long userId = currentUserIdResolver.resolve(authentication);
        return ApiResponse.success(matchingService.updateStatus(userId, matchingId, request));
    }

    private PageResponse<MatchingResponse> toPageResponse(Page<MatchingResponse> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
    }
}
