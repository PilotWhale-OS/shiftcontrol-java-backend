package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.BadRequestException;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.TimeConstraint;
import at.shiftcontrol.shiftservice.mapper.TimeConstraintMapper;
import at.shiftcontrol.shiftservice.service.TimeConstraintService;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

@Service
@RequiredArgsConstructor
public class TimeConstraintServiceImpl implements TimeConstraintService {
    private final TimeConstraintDao timeConstraintDao;
    private final AssignmentDao assignmentDao;
    private final EventDao eventDao;
    private final VolunteerDao volunteerDao;

    @Override
    public Collection<TimeConstraintDto> getTimeConstraints(@NonNull String userId, long eventId) {
        return TimeConstraintMapper.toDto(timeConstraintDao.searchByVolunteerAndEvent(userId, eventId));
    }

    @Override
    public TimeConstraintDto createTimeConstraint(@NonNull TimeConstraintCreateDto createDto, @NonNull String userId, long eventId) throws ConflictException {
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
        var event = eventDao.findById(eventId).orElseThrow(() -> new ConflictException("Event not found with id: " + eventId));
        var volunteer = volunteerDao.findById(userId).orElseThrow(() -> new ConflictException("Volunteer not found with id: " + userId));

        // Check that volunteer is part of the event via any of their shift plans
        var userPlans = volunteer.getVolunteeringPlans();
        var volunteerIsInEvent = userPlans != null && userPlans.stream().anyMatch(plan -> plan.getEvent().getId() == eventId);
        if (!volunteerIsInEvent) {
            throw new ConflictException("Volunteer with id: %s is not part of event with id: %d".formatted(userId, eventId));
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

        //TODO publish event
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
    public void delete(long timeConstraintId) throws NotFoundException {
        Optional<TimeConstraint> atcOpt = timeConstraintDao.findById(timeConstraintId);
        if (atcOpt.isEmpty()) {
            throw new NotFoundException("Time constraint not found");
        }

        //TODO publish event
        timeConstraintDao.delete(atcOpt.get());
    }

    static void checkForConstraintOverlaps(@NonNull TimeConstraintCreateDto createDto,
                                           @NonNull Collection<TimeConstraint> existingConstraints) throws ConflictException {
        for (var constraint : existingConstraints) {
            if (createDto.getFrom().isBefore(constraint.getEndTime())
                && createDto.getTo().isAfter(constraint.getStartTime())) {
                throw new ConflictException("New time constraint overlaps with existing time constraint id=%d".formatted(constraint.getId()));
            }
        }
    }

    private void checkForAssignmentOverlaps(@NonNull String userId, Instant from, Instant to) throws ConflictException {
        var existingAssignments = assignmentDao.getConflictingAssignments(userId, from, to);
        if (!existingAssignments.isEmpty()) {
            throw new ConflictException("New time constraint overlaps with existing assignments ids=%s"
                .formatted(existingAssignments.stream().map(Assignment::getId).toList().toString()));
        }
    }
}
