package com.gymchin.api.matching.service;

import com.gymchin.api.common.dto.PageResponse;
import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import com.gymchin.api.matching.entity.MatchingStatus;
import com.gymchin.api.matching.repository.MatchingRepository;
import com.gymchin.api.user.dto.GymmateSearchCriteria;
import com.gymchin.api.user.dto.UserSummaryDto;
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
@Transactional(readOnly = true)
public class GymmateService {

    private static final EnumSet<MatchingStatus> EXCLUDED_STATUSES = EnumSet.of(
        MatchingStatus.REQUESTED, MatchingStatus.ACCEPTED
    );

    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;

    public GymmateService(UserRepository userRepository, MatchingRepository matchingRepository) {
        this.userRepository = userRepository;
        this.matchingRepository = matchingRepository;
    }

    public PageResponse<UserSummaryDto> searchGymmates(Long requesterUserId, GymmateSearchCriteria criteria,
                                                       Pageable pageable) {
        User requester = userRepository.findById(requesterUserId)
            .orElseThrow(() -> AppException.of(ErrorCode.RESOURCE_NOT_FOUND, "Requester not found", HttpStatus.NOT_FOUND));

        List<Long> excludedUserIds = matchingRepository.findRelatedUserIds(requesterUserId, EXCLUDED_STATUSES);
        Page<User> page = userRepository.searchGymmates(
            requesterUserId,
            requester.getGymName(),
            criteria,
            excludedUserIds,
            pageable
        );

        return new PageResponse<>(
            page.map(this::toSummary).getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
    }

    private UserSummaryDto toSummary(User user) {
        return new UserSummaryDto(
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
    }
}
