package at.shiftcontrol.shiftservice.dao.impl.specification;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

public final class ShiftSpecifications {
    private ShiftSpecifications() {
    }

    public static Specification<Shift> inShiftPlan(long shiftPlanId) {
        // shiftPlan-related restriction: Shift -> shiftPlan.id
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("shiftPlan").get("id"), shiftPlanId);
    }

    public static Specification<Shift> assignedToUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true); // important because of joins to collections

            // user-related restriction: Shift -> slots -> assignments -> assignedVolunteer.id
            var slots = root.join("slots", JoinType.INNER);
            var assignments = slots.join("assignments", JoinType.INNER);
            var volunteer = assignments.join("assignedVolunteer", JoinType.INNER);

            return criteriaBuilder.equal(volunteer.get("id"), userId);
        };
    }

    public static Specification<Shift> signupPossibleForUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            // user is NOT already assigned in this shift
            var notAssignedSub = query.subquery(Long.class);
            var s1 = notAssignedSub.from(Shift.class);
            var sl1 = s1.join("slots", JoinType.INNER);
            var a1 = sl1.join("assignments", JoinType.INNER);
            var v1 = a1.join("assignedVolunteer", JoinType.INNER);

            notAssignedSub.select(criteriaBuilder.literal(1L))
                .where(
                    criteriaBuilder.equal(s1.get("id"), root.get("id")),
                    criteriaBuilder.equal(v1.get("id"), userId)
                );

            var userNotAssigned = criteriaBuilder.not(criteriaBuilder.exists(notAssignedSub));

            // exists a slot with capacity OR slot is up for auction
            var slotExistsSub = query.subquery(Long.class);
            var s2 = slotExistsSub.from(Shift.class);
            var slot = s2.join("slots", JoinType.INNER);

            // 2a) capacity: desiredVolunteerCount > count(filled)
            var filledCountSub = query.subquery(Long.class);
            var slot3 = filledCountSub.from(PositionSlot.class);
            var a3 = slot3.join("assignments", JoinType.LEFT);

            filledCountSub.select(criteriaBuilder.count(a3))
                .where(
                    criteriaBuilder.equal(slot3.get("id"), slot.get("id")),
                    criteriaBuilder.isNotNull(a3.get("assignedVolunteer"))
                );

            var hasCapacity = criteriaBuilder.greaterThan(
                slot.get("desiredVolunteerCount"),
                filledCountSub
            );

            // 2b) auction exists on that slot
            var auctionExistsSub = query.subquery(Long.class);
            var slot4 = auctionExistsSub.from(PositionSlot.class);
            var a4 = slot4.join("assignments", JoinType.INNER);

            auctionExistsSub.select(criteriaBuilder.literal(1L))
                .where(
                    criteriaBuilder.equal(slot4.get("id"), slot.get("id")),
                    criteriaBuilder.equal(a4.get("status"), AssignmentStatus.AUCTION)
                );

            var hasAuction = criteriaBuilder.exists(auctionExistsSub);

            slotExistsSub.select(criteriaBuilder.literal(1L))
                .where(
                    criteriaBuilder.equal(s2.get("id"), root.get("id")),
                    criteriaBuilder.or(hasCapacity, hasAuction)
                );

            var slotAllowsSignup = criteriaBuilder.exists(slotExistsSub);

            return criteriaBuilder.and(userNotAssigned, slotAllowsSignup);
        };
    }

    public static Specification<Shift> matchesSearchDto(ShiftPlanScheduleSearchDto searchDto) {
        return (root, query, criteriaBuilder) -> {
            if (searchDto == null) {
                return criteriaBuilder.conjunction();
            }

            var predicates = criteriaBuilder.conjunction();

            // date filter (LocalDate -> Instant range)
            if (searchDto.getDate() != null) {
                Instant dayStart = TimeUtil.convertToStartOfUtcDayInstant(searchDto.getDate());
                Instant dayEnd = TimeUtil.convertToEndOfUtcDayInstant(searchDto.getDate());

                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.between(root.get("startTime"), dayStart, dayEnd));
            }

            // shift name contains (case-insensitive)
            if (searchDto.getShiftName() != null && !searchDto.getShiftName().isBlank()) {
                String pattern = "%" + searchDto.getShiftName().toLowerCase() + "%";
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        pattern
                    ));
            }

            // roleNames: Shift -> slots --> role --> name
            if (searchDto.getRoleNames() != null && !searchDto.getRoleNames().isEmpty()) {
                var roleJoin = root
                    .join("slots", JoinType.INNER)
                    .join("role", JoinType.INNER);

                var likePredicates = searchDto.getRoleNames().stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(r -> "%" + r.toLowerCase() + "%")
                    .map(pattern ->
                        criteriaBuilder.like(
                            criteriaBuilder.lower(roleJoin.get("name")),
                            pattern
                        )
                    )
                    .toArray(Predicate[]::new);

                // Only add if we actually have patterns
                if (likePredicates.length > 0) {
                    predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.or(likePredicates)
                    );
                }
            }

            // locations: Shift -> locations --> name
            if (searchDto.getLocations() != null && !searchDto.getLocations().isEmpty()) {
                var locationJoin = root.join("location", JoinType.INNER);

                var likePredicates = searchDto.getLocations().stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(s -> "%" + s.toLowerCase() + "%")
                    .map(pattern ->
                        criteriaBuilder.like(
                            criteriaBuilder.lower(locationJoin.get("name")),
                            pattern
                        )
                    )
                    .toArray(Predicate[]::new);

                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.or(likePredicates)
                );
            }

            return predicates;
        };
    }
}

