package com.gymchin.api.user.repository;

import com.gymchin.api.user.dto.GymmateSearchCriteria;
import com.gymchin.api.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final EntityManager entityManager;

    public UserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<User> searchGymmates(Long requesterUserId, String requesterGymName, GymmateSearchCriteria criteria,
                                     List<Long> excludedUserIds, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        List<Predicate> predicates = buildPredicates(cb, root, criteria, requesterUserId, excludedUserIds);
        query.where(predicates.toArray(new Predicate[0])).distinct(true);

        if (requesterGymName != null && !requesterGymName.isBlank()) {
            Expression<Integer> gymOrder = cb.<Integer>selectCase()
                .when(cb.equal(root.get("gymName"), requesterGymName), 0)
                .otherwise(1);
            query.orderBy(
                cb.asc(gymOrder),
                cb.desc(root.get("updatedAt")),
                cb.desc(root.get("createdAt"))
            );
        } else {
            query.orderBy(
                cb.desc(root.get("updatedAt")),
                cb.desc(root.get("createdAt"))
            );
        }

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<User> results = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, criteria, requesterUserId, excludedUserIds);
        countQuery.select(cb.countDistinct(countRoot))
            .where(countPredicates.toArray(new Predicate[0]));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<User> root, GymmateSearchCriteria criteria,
                                            Long requesterUserId, List<Long> excludedUserIds) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.notEqual(root.get("id"), requesterUserId));

        if (excludedUserIds != null && !excludedUserIds.isEmpty()) {
            predicates.add(cb.not(root.get("id").in(excludedUserIds)));
        }

        if (criteria.getCity() != null) {
            predicates.add(cb.equal(root.get("city"), criteria.getCity()));
        }
        if (criteria.getDistrict() != null) {
            predicates.add(cb.equal(root.get("district"), criteria.getDistrict()));
        }
        if (criteria.getFitnessLevel() != null) {
            predicates.add(cb.equal(root.get("fitnessLevel"), criteria.getFitnessLevel()));
        }
        if (criteria.getCoachOption() != null) {
            predicates.add(cb.equal(root.get("coachOption"), criteria.getCoachOption()));
        }
        if (criteria.getGoal() != null) {
            Join<User, String> goalsJoin = root.join("goals");
            predicates.add(cb.equal(goalsJoin, criteria.getGoal()));
        }
        if (criteria.getDay() != null) {
            Join<User, String> daysJoin = root.join("preferredDays");
            predicates.add(cb.equal(daysJoin, criteria.getDay()));
        }
        if (criteria.getTimeSlot() != null) {
            Join<User, String> timeSlotsJoin = root.join("preferredTimeSlots");
            predicates.add(cb.equal(timeSlotsJoin, criteria.getTimeSlot()));
        }

        return predicates;
    }
}
