package at.shiftcontrol.auditlog.dao.specification;

import org.springframework.data.jpa.domain.Specification;

import at.shiftcontrol.auditlog.dto.LogSearchDto;
import at.shiftcontrol.auditlog.entity.LogEntry;

public final class LogEntrySpecifications {
    private LogEntrySpecifications() {
    }

    public static Specification<LogEntry> matchesSearchDto(LogSearchDto searchDto) {
        return (root, query, criteriaBuilder) -> {
            if (searchDto == null) {
                return criteriaBuilder.conjunction();
            }

            var predicates = criteriaBuilder.conjunction();

            // time range filter
            if (searchDto.getStartTime() != null && searchDto.getEndTime() != null) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.between(root.get("timestamp"), searchDto.getStartTime(), searchDto.getEndTime())
                );
            }

            // acting user ID
            if (searchDto.getActingUserId() != null && !searchDto.getActingUserId().isBlank()) {
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("actingUserId"), searchDto.getActingUserId())
                );
            }

            // event type substring match
            if (searchDto.getEventType() != null && !searchDto.getEventType().isBlank()) {
                String pattern = "%" + searchDto.getEventType().toLowerCase().trim() + "%";
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("eventType")),
                        pattern
                    ));
            }

            // routing key substring match
            if (searchDto.getRoutingKey() != null && !searchDto.getRoutingKey().isBlank()) {
                String pattern = "%" + searchDto.getRoutingKey().toLowerCase().trim() + "%";
                predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("routingKey")),
                        pattern
                    ));
            }
            query.orderBy(criteriaBuilder.desc(root.get("timestamp")));

            return predicates;
        };
    }
}

