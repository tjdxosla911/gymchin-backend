package com.gymchin.api.user.repository;

import com.gymchin.api.user.dto.GymmateSearchCriteria;
import com.gymchin.api.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchGymmates(Long requesterUserId, String requesterGymName, GymmateSearchCriteria criteria,
                              List<Long> excludedUserIds, Pageable pageable);
}
