package at.shiftcontrol.shiftservice.dao.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import at.shiftcontrol.lib.util.TimeUtil;
import at.shiftcontrol.shiftservice.dao.ShiftDao;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.repo.ShiftRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShiftDaoImpl implements ShiftDao {
    private final ShiftRepository shiftRepository;

    @Override
    public Optional<Shift> findById(Long id) {
        return shiftRepository.findById(id);
    }

    @Override
    public Shift save(Shift entity) {
        return shiftRepository.save(entity);
    }

    @Override
    public void delete(Shift entity) {
        shiftRepository.delete(entity);
    }


    @Override
    public List<Shift> searchUserRelatedShiftsInShiftPlan(long shiftPlanId, long userId, ShiftPlanScheduleSearchDto searchDto) {
        return shiftRepository.findAll((Specification<Shift>) (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // shiftPlan-related restriction: Shift -> shiftPlan.id
            predicates = criteriaBuilder.and(predicates,
                criteriaBuilder.equal(root.get("shiftPlan").get("id"), shiftPlanId));

            // user-related restriction: Shift -> slots -> assignments -> assignedVolunteer.id
            var slotsJoin = root.join("slots", JoinType.INNER);
            var assignmentsJoin = slotsJoin.join("assignments", JoinType.INNER);
            var volunteerJoin = assignmentsJoin.join("assignedVolunteer", JoinType.INNER);

            predicates = criteriaBuilder.and(predicates,
                criteriaBuilder.equal(volunteerJoin.get("id"), userId));

            if (searchDto == null) {
                return predicates;
            }

            // --- Apply additional filters from searchDto ---
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

        });
    }
}
