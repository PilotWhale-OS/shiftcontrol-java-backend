package at.shiftcontrol.shiftservice.dao.impl.specification;

import java.time.Instant;
import java.time.ZoneOffset;

import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import jakarta.persistence.criteria.JoinType;
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

    public static Specification<Shift> matchesSearchDto(ShiftPlanScheduleFilterDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            if (filterDto == null) {
                return criteriaBuilder.conjunction();
            }

            var predicates = criteriaBuilder.conjunction();

            // date filter (LocalDate -> Instant range)
            if (filterDto instanceof ShiftPlanScheduleDaySearchDto daySearchDto && daySearchDto.getDate() != null) {
                Instant dayStart = TimeUtil.convertToStartOfUtcDayInstant(daySearchDto.getDate());
                Instant nextDayStart = daySearchDto.getDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.lessThan(root.get("startTime"), nextDayStart),
                    criteriaBuilder.greaterThan(root.get("endTime"), dayStart)
                );
            }

            // shift name contains (case-insensitive)
            if (filterDto.getShiftName() != null && !filterDto.getShiftName().isBlank()) {
                String pattern = "%" + filterDto.getShiftName().toLowerCase() + "%";
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        pattern
                    ));
            }

            // roleNames: Shift -> slots --> role --> id
            if (filterDto.getRoleIds() != null && !filterDto.getRoleIds().isEmpty()) {
                var roleJoin = root
                    .join("slots", JoinType.INNER)
                    .join("role", JoinType.INNER);


                // filter only non-null, non-blank role IDs
                predicates = criteriaBuilder.and(predicates,
                    roleJoin.get("id").in(
                        filterDto.getRoleIds().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .toList()
                    )
                );
            }

            // locations: Shift -> locations --> id
            if (filterDto.getLocationIds() != null && !filterDto.getLocationIds().isEmpty()) {
                var locationJoin = root.join("location", JoinType.INNER);

                // filter only non-null, non-blank location IDs
                predicates = criteriaBuilder.and(predicates,
                    locationJoin.get("id").in(
                        filterDto.getLocationIds().stream()
                            .filter(s -> s != null && !s.isBlank())
                            .toList()
                    )
                );
            }

            return predicates;
        };
    }
}

