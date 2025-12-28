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
import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AttendanceId;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import at.shiftcontrol.shiftservice.mapper.TimeConstraintMapper;
import at.shiftcontrol.shiftservice.service.TimeConstraintService;
import at.shiftcontrol.shiftservice.type.TimeConstraintType;

@Service
@RequiredArgsConstructor
public class TimeConstraintServiceImpl implements TimeConstraintService {
    private final AttendanceTimeConstraintDao attendanceTimeConstraintDao;
    private final AttendanceDao attendanceDao;
    private final AssignmentDao assignmentDao;

    @Override
    public Collection<TimeConstraintDto> getTimeConstraints(@NonNull String userId, long eventId) {
        return TimeConstraintMapper.toDto(attendanceTimeConstraintDao.searchByVolunteerAndEvent(userId, eventId));
    }

    @Override
    public TimeConstraintDto createTimeConstraint(@NonNull TimeConstraintCreateDto createDto, @NonNull String userId, long eventId) throws ConflictException {
        // Validate date range
        Instant from = createDto.getFrom();
        Instant to = createDto.getTo();
        if (from == null || to == null || !from.isBefore(to)) {
            throw new ConflictException("Invalid time range: 'from' must be before 'to'");
        }
        // Resolve attendance (userId is volunteerId)
        var attendanceId = AttendanceId.of(userId, eventId);
        var attendance = attendanceDao.findById(attendanceId).orElseThrow(() -> new ConflictException(
            "Cannot create time constraint because attendance for volunteerId=%s and eventId=%d does not exist"
                .formatted(userId, eventId)
        ));
        switch (createDto.getType()) {
            case UNAVAILABLE -> {
                // Check for overlapping time constraints for this volunteer+event
                var existingConstraints = attendanceTimeConstraintDao.searchByVolunteerAndEvent(userId, eventId);
                checkForConstraintOverlaps(createDto, existingConstraints);
                checkForAssignmentOverlaps(userId, from, to);
            }
            case EMERGENCY -> {
                validateEmergencyWholeDays(from, to);
                // Check for overlapping time constraints for this volunteer+event+type
                var existingConstraints = attendanceTimeConstraintDao.searchByVolunteerAndEventAndType(userId, eventId, TimeConstraintType.EMERGENCY);
                checkForConstraintOverlaps(createDto, existingConstraints);
                // todo add trust allert
            }
            default -> throw new IllegalStateException("Unexpected value: " + createDto.getType());
        }
        var entity = attendanceTimeConstraintDao.save(TimeConstraintMapper.fromCreateDto(createDto, attendance));
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
        Optional<AttendanceTimeConstraint> atcOpt = attendanceTimeConstraintDao.findById(timeConstraintId);
        if (atcOpt.isEmpty()) {
            throw new NotFoundException("Time constraint not found");
        }
        attendanceTimeConstraintDao.delete(atcOpt.get());
    }

    static void checkForConstraintOverlaps(@NonNull TimeConstraintCreateDto createDto,
                                           @NonNull Collection<AttendanceTimeConstraint> existingConstraints) throws ConflictException {
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
