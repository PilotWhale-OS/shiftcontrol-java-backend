package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.TimeConstraint;
import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.type.TimeConstraintType;
import at.shiftcontrol.shiftservice.annotation.IsNotAdmin;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.TimeConstraintEvent;
import at.shiftcontrol.shiftservice.mapper.TimeConstraintMapper;
import at.shiftcontrol.shiftservice.service.TimeConstraintService;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeConstraintServiceImpl implements TimeConstraintService {
    private final TimeConstraintDao timeConstraintDao;
    private final AssignmentDao assignmentDao;
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationEventPublisher publisher;
    private final SecurityHelper securityHelper;

    @Override
    public Collection<TimeConstraintDto> getTimeConstraints(@NonNull String userId, long eventId) {
        return TimeConstraintMapper.toDto(timeConstraintDao.searchByVolunteerAndEvent(userId, eventId));
    }

    @Override
    @IsNotAdmin
    public TimeConstraintDto createTimeConstraint(@NonNull TimeConstraintCreateDto createDto, @NonNull String userId, long eventId) {
        // todo add security
        // Validate date range
        Instant from = createDto.getFrom();
        Instant to = createDto.getTo();
        var type = createDto.getType();
        if (from == null || to == null || type == TimeConstraintType.UNAVAILABLE && !from.isBefore(to)) {
            throw new ConflictException("Invalid time range: 'from' must be before 'to' for unavailable type");
        }
        if (type == TimeConstraintType.EMERGENCY && from.isAfter(to)) {
            throw new ConflictException("Invalid time whole day range: 'from' must at least be 'to' for emergency type");
        }
        // get event and volunteer
        var event = eventDao.getById(eventId);
        var volunteer = volunteerDao.getById(userId);
        // Check that volunteer is part of the event via any of their shift plans
        var userPlans = volunteer.getVolunteeringPlans();
        var volunteerIsInEvent = userPlans != null && userPlans.stream().anyMatch(plan -> plan.getEvent().getId() == eventId);
        if (!volunteerIsInEvent) {
            log.error("Volunteer with id: {} is not part of event with id: {}", userId, eventId);
            throw new ConflictException("Volunteer is not part of event.");
        }
        switch (createDto.getType()) {
            case UNAVAILABLE -> {
                // Check for overlapping time constraints for this volunteer+event
                var existingConstraints = timeConstraintDao.searchByVolunteerAndEvent(userId, eventId);
                checkForConstraintOverlaps(createDto, existingConstraints);
                checkForAssignmentOverlaps(userId, from, to);
            }
            case EMERGENCY -> {
                validateEmergencyWholeDays(from, to);
                // Check for overlapping time constraints for this volunteer+event+type
                var existingConstraints = timeConstraintDao.searchByVolunteerAndEventAndType(userId, eventId, TimeConstraintType.EMERGENCY);
                checkForConstraintOverlaps(createDto, existingConstraints);
                // todo add trust alert
            }
            default -> throw new IllegalStateException("Unexpected value: " + createDto.getType());
        }
        var entity = timeConstraintDao.save(TimeConstraintMapper.fromCreateDto(createDto, volunteer, event));
        publisher.publishEvent(TimeConstraintEvent.of(RoutingKeys.TIMECONSTRAINT_CREATED, entity));
        return TimeConstraintMapper.toDto(entity);
    }

    private static void validateEmergencyWholeDays(Instant from, Instant to) {
        boolean fromIsMidnightUtc = from.atZone(ZoneOffset.UTC).toLocalTime().equals(LocalTime.MIDNIGHT);
        boolean toIsMidnightUtc = to.atZone(ZoneOffset.UTC).toLocalTime().equals(LocalTime.MIDNIGHT);
        if (!fromIsMidnightUtc || !toIsMidnightUtc) {
            throw new BadRequestException("For EMERGENCY constraints, 'from' and 'to' must be whole days (00:00 UTC).");
        }
    }

    @Override
    @IsNotAdmin
    public void delete(long timeConstraintId) {
        var timeConstraint = timeConstraintDao.getById(timeConstraintId);
        timeConstraintDao.delete(timeConstraint);
        publisher.publishEvent(TimeConstraintEvent.of(RoutingKeys.format(RoutingKeys.TIMECONSTRAINT_DELETED,
            Map.of("timeConstraintId", String.valueOf(timeConstraintId), "volunteerId", timeConstraint.getVolunteer().getId())), timeConstraint));
    }

    static void checkForConstraintOverlaps(@NonNull TimeConstraintCreateDto createDto,
                                           @NonNull Collection<TimeConstraint> existingConstraints) {
        for (var constraint : existingConstraints) {
            if (createDto.getFrom().isBefore(constraint.getEndTime())
                && createDto.getTo().isAfter(constraint.getStartTime())) {
                log.error("New time constraint from {} to {} overlaps with existing time constraint id={} from {} to {}",
                    createDto.getFrom(), createDto.getTo(),
                    constraint.getId(), constraint.getStartTime(), constraint.getEndTime());
                throw new ConflictException("New time constraint overlaps with existing time constraint.");
            }
        }
    }

    private void checkForAssignmentOverlaps(@NonNull String userId, Instant from, Instant to) {
        var existingAssignments = assignmentDao.getConflictingAssignments(userId, from, to);
        if (!existingAssignments.isEmpty()) {
            log.error("New time constraint from {} to {} overlaps with existing assignments ids={}",
                from, to,
                existingAssignments.stream().map(Assignment::getId).toList());
            throw new ConflictException("New time constraint overlaps with existing assignments.");
        }
    }
}
