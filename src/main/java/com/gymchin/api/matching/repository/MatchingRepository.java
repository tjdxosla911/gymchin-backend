package com.gymchin.api.matching.repository;

import com.gymchin.api.matching.entity.Matching;
import com.gymchin.api.matching.entity.MatchingStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    @Query("""
        select m from Matching m
        where ((m.requester.id = :userId1 and m.target.id = :userId2)
            or (m.requester.id = :userId2 and m.target.id = :userId1))
            and m.status in :statuses
        """)
    List<Matching> findByUserPairAndStatuses(@Param("userId1") Long userId1,
                                             @Param("userId2") Long userId2,
                                             @Param("statuses") Collection<MatchingStatus> statuses);

    @Query("""
        select distinct
            case
                when m.requester.id = :userId then m.target.id
                else m.requester.id
            end
        from Matching m
        where (m.requester.id = :userId or m.target.id = :userId)
          and m.status in :statuses
        """)
    List<Long> findRelatedUserIds(@Param("userId") Long userId,
                                  @Param("statuses") Collection<MatchingStatus> statuses);

    Page<Matching> findByRequesterId(Long requesterId, Pageable pageable);

    Page<Matching> findByRequesterIdAndStatus(Long requesterId, MatchingStatus status, Pageable pageable);

    Page<Matching> findByTargetId(Long targetId, Pageable pageable);

    Page<Matching> findByTargetIdAndStatus(Long targetId, MatchingStatus status, Pageable pageable);

    Optional<Matching> findById(Long id);
}
