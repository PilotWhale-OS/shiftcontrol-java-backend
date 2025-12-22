package at.shiftcontrol.shiftservice.dao.impl.specification;

import java.time.Instant;

import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

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

