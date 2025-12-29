package com.gymchin.api.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import com.gymchin.api.matching.dto.MatchingCreateRequest;
import com.gymchin.api.matching.dto.MatchingResponse;
import com.gymchin.api.matching.dto.MatchingStatusUpdateRequest;
import com.gymchin.api.matching.entity.MatchingStatus;
import com.gymchin.api.matching.service.MatchingService;
import com.gymchin.api.user.entity.User;
import com.gymchin.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void preventsSelfRequest() {
        User user = createUser("self@example.com", "self");
        MatchingCreateRequest request = new MatchingCreateRequest();
        request.setTargetUserId(user.getId());

        assertThatThrownBy(() -> matchingService.createMatching(user.getId(), request))
            .isInstanceOf(AppException.class)
            .extracting(ex -> ((AppException) ex).getErrorCode())
            .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void preventsDuplicateMatchingRequest() {
        User requester = createUser("r@example.com", "req");
        User target = createUser("t@example.com", "tar");

        MatchingCreateRequest request = new MatchingCreateRequest();
        request.setTargetUserId(target.getId());
        matchingService.createMatching(requester.getId(), request);

        assertThatThrownBy(() -> matchingService.createMatching(requester.getId(), request))
            .isInstanceOf(AppException.class)
            .extracting(ex -> ((AppException) ex).getErrorCode())
            .isEqualTo(ErrorCode.MATCH_ALREADY_REQUESTED);
    }

    @Test
    void enforcesStatusTransitionRules() {
        User requester = createUser("a@example.com", "alpha");
        User target = createUser("b@example.com", "beta");

        MatchingCreateRequest request = new MatchingCreateRequest();
        request.setTargetUserId(target.getId());
        MatchingResponse response = matchingService.createMatching(requester.getId(), request);

        MatchingStatusUpdateRequest invalidByRequester = new MatchingStatusUpdateRequest();
        invalidByRequester.setStatus(MatchingStatus.ACCEPTED);

        assertThatThrownBy(() -> matchingService.updateStatus(requester.getId(), response.getMatchingId(), invalidByRequester))
            .isInstanceOf(AppException.class)
            .extracting(ex -> ((AppException) ex).getErrorCode())
            .isEqualTo(ErrorCode.AUTH_UNAUTHORIZED);

        MatchingStatusUpdateRequest acceptRequest = new MatchingStatusUpdateRequest();
        acceptRequest.setStatus(MatchingStatus.ACCEPTED);
        MatchingResponse accepted = matchingService.updateStatus(target.getId(), response.getMatchingId(), acceptRequest);
        assertThat(accepted.getStatus()).isEqualTo(MatchingStatus.ACCEPTED);

        MatchingStatusUpdateRequest invalidTransition = new MatchingStatusUpdateRequest();
        invalidTransition.setStatus(MatchingStatus.REJECTED);

        assertThatThrownBy(() -> matchingService.updateStatus(target.getId(), response.getMatchingId(), invalidTransition))
            .isInstanceOf(AppException.class)
            .extracting(ex -> ((AppException) ex).getErrorCode())
            .isEqualTo(ErrorCode.MATCH_INVALID_STATE);
    }

    private User createUser(String email, String nickname) {
        User user = new User(email, nickname);
        return userRepository.save(user);
    }
}
