package com.gymchin.api.matching.service;

import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import com.gymchin.api.matching.dto.MatchingCreateRequest;
import com.gymchin.api.matching.dto.MatchingResponse;
import com.gymchin.api.matching.dto.MatchingStatusUpdateRequest;
import com.gymchin.api.matching.entity.Matching;
import com.gymchin.api.matching.entity.MatchingStatus;
import com.gymchin.api.matching.repository.MatchingRepository;
import com.gymchin.api.user.entity.User;
import com.gymchin.api.user.repository.UserRepository;
import java.util.EnumSet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchingService {

    private static final EnumSet<MatchingStatus> ACTIVE_STATUSES = EnumSet.of(
        MatchingStatus.REQUESTED, MatchingStatus.ACCEPTED
    );

    private enum ActorRole {
        REQUESTER,
        TARGET
    }

    private final MatchingRepository matchingRepository;
    private final UserRepository userRepository;

    public MatchingService(MatchingRepository matchingRepository, UserRepository userRepository) {
        this.matchingRepository = matchingRepository;
        this.userRepository = userRepository;
    }

    public MatchingResponse createMatching(Long requesterUserId, MatchingCreateRequest request) {
        if (requesterUserId.equals(request.getTargetUserId())) {
            throw AppException.of(ErrorCode.CONFLICT, "Cannot request matching to self", HttpStatus.CONFLICT);
        }

        User requester = userRepository.findById(requesterUserId)
            .orElseThrow(() -> AppException.of(ErrorCode.RESOURCE_NOT_FOUND, "Requester not found", HttpStatus.NOT_FOUND));
        User target = userRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> AppException.of(ErrorCode.RESOURCE_NOT_FOUND, "Target user not found", HttpStatus.NOT_FOUND));

        List<Matching> existing = matchingRepository.findByUserPairAndStatuses(requesterUserId, request.getTargetUserId(),
            ACTIVE_STATUSES);
        if (!existing.isEmpty()) {
            boolean hasAccepted = existing.stream().anyMatch(m -> m.getStatus() == MatchingStatus.ACCEPTED);
            ErrorCode code = hasAccepted ? ErrorCode.MATCH_ALREADY_CONNECTED : ErrorCode.MATCH_ALREADY_REQUESTED;
            String message = hasAccepted ? "Already connected" : "Matching already requested";
            throw AppException.of(code, message, HttpStatus.CONFLICT);
        }

        Matching matching = new Matching(requester, target, MatchingStatus.REQUESTED, request.getMessage());
        Matching saved = matchingRepository.save(matching);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<MatchingResponse> getSentMatchings(Long requesterUserId, MatchingStatus status, Pageable pageable) {
        Page<Matching> page = status == null
            ? matchingRepository.findByRequesterId(requesterUserId, pageable)
            : matchingRepository.findByRequesterIdAndStatus(requesterUserId, status, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MatchingResponse> getReceivedMatchings(Long targetUserId, MatchingStatus status, Pageable pageable) {
        Page<Matching> page = status == null
            ? matchingRepository.findByTargetId(targetUserId, pageable)
            : matchingRepository.findByTargetIdAndStatus(targetUserId, status, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MatchingResponse getMatching(Long userId, Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
            .orElseThrow(() -> AppException.of(ErrorCode.RESOURCE_NOT_FOUND, "Matching not found", HttpStatus.NOT_FOUND));
        ensureParticipant(userId, matching);
        return toResponse(matching);
    }

    public MatchingResponse updateStatus(Long userId, Long matchingId, MatchingStatusUpdateRequest request) {
        Matching matching = matchingRepository.findById(matchingId)
            .orElseThrow(() -> AppException.of(ErrorCode.RESOURCE_NOT_FOUND, "Matching not found", HttpStatus.NOT_FOUND));

        MatchingStatus current = matching.getStatus();
        MatchingStatus next = request.getStatus();
        ActorRole role = resolveActorRole(userId, matching);
        validateTransition(current, next, role);
        matching.setStatus(next);
        return toResponse(matching);
    }

    private void ensureParticipant(Long userId, Matching matching) {
        if (!matching.getRequester().getId().equals(userId) && !matching.getTarget().getId().equals(userId)) {
            throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Forbidden", HttpStatus.FORBIDDEN);
        }
    }

    private ActorRole resolveActorRole(Long userId, Matching matching) {
        if (matching.getRequester().getId().equals(userId)) {
            return ActorRole.REQUESTER;
        }
        if (matching.getTarget().getId().equals(userId)) {
            return ActorRole.TARGET;
        }
        throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Forbidden", HttpStatus.FORBIDDEN);
    }

    private void validateTransition(MatchingStatus current, MatchingStatus next, ActorRole role) {
        if (current == MatchingStatus.REJECTED || current == MatchingStatus.CANCELLED || current == MatchingStatus.ENDED) {
            throw AppException.of(ErrorCode.MATCH_INVALID_STATE, "Matching already closed", HttpStatus.CONFLICT);
        }
        if (current == MatchingStatus.REQUESTED) {
            if (next == MatchingStatus.ACCEPTED || next == MatchingStatus.REJECTED) {
                if (role != ActorRole.TARGET) {
                    throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Only target user can respond", HttpStatus.FORBIDDEN);
                }
                return;
            }
            if (next == MatchingStatus.CANCELLED) {
                if (role != ActorRole.REQUESTER) {
                    throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Only requester can cancel", HttpStatus.FORBIDDEN);
                }
                return;
            }
            throw AppException.of(ErrorCode.MATCH_INVALID_STATE, "Invalid status transition", HttpStatus.CONFLICT);
        }
        if (current == MatchingStatus.ACCEPTED) {
            if (next != MatchingStatus.ENDED) {
                throw AppException.of(ErrorCode.MATCH_INVALID_STATE, "Invalid status transition", HttpStatus.CONFLICT);
            }
            return;
        }
        throw AppException.of(ErrorCode.MATCH_INVALID_STATE, "Invalid status transition", HttpStatus.CONFLICT);
    }

    private MatchingResponse toResponse(Matching matching) {
        return new MatchingResponse(
            matching.getId(),
            matching.getRequester().getId(),
            matching.getTarget().getId(),
            matching.getStatus(),
            matching.getMessage(),
            matching.getCreatedAt(),
            matching.getUpdatedAt()
        );
    }
}
