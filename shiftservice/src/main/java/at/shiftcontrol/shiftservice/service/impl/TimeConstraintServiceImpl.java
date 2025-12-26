package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dao.AttendanceDao;
import at.shiftcontrol.shiftservice.dao.AttendanceTimeConstraintDao;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceId;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;
import at.shiftcontrol.shiftservice.mapper.TimeConstraintMapper;
import at.shiftcontrol.shiftservice.service.TimeConstraintService;

@Service
@RequiredArgsConstructor
public class TimeConstraintServiceImpl implements TimeConstraintService {
    private final AttendanceTimeConstraintDao attendanceTimeConstraintDao;
    private final AttendanceDao attendanceDao;

    @Override
    public Collection<TimeConstraintDto> getTimeConstraints(String userId, long eventId) {
        return TimeConstraintMapper.toDto(attendanceTimeConstraintDao.searchByVolunteerAndEvent(userId, eventId));
    }

    @Override
    public TimeConstraintDto createTimeConstraint(TimeConstraintCreateDto createDto, String userId, long eventId) throws ConflictException {
        // Validate date range
        if (createDto.getFrom() == null || createDto.getTo() == null || !createDto.getFrom().isBefore(createDto.getTo())) {
            throw new ConflictException("Invalid time range: 'from' must be before 'to'");
        }

        // Resolve attendance (userId is volunteerId)
        AttendanceId attendanceId = AttendanceId.of(userId, eventId);
        Optional<Attendance> attendanceOpt = attendanceDao.findById(attendanceId);
        if (attendanceOpt.isEmpty()) {
            throw new ConflictException(
                "Cannot create time constraint because attendance for volunteerId=%s and eventId=%d does not exist"
                    .formatted(userId, eventId));
        }
        Attendance attendance = attendanceOpt.get();

        // Check for overlapping time constraints for this volunteer+event
        var existing = attendanceTimeConstraintDao.searchByVolunteerAndEvent(userId, eventId);
        for (AttendanceTimeConstraint ex : existing) {
            // overlap if start < ex.end && end > ex.start
            if (!(createDto.getTo().isBefore(ex.getStartTime()) || createDto.getFrom().isAfter(ex.getEndTime()))) {
                throw new ConflictException("Time constraint overlaps with existing constraint");
            }
        }

        AttendanceTimeConstraint entity = AttendanceTimeConstraint.builder()
            .attendance(attendance)
            .type(createDto.getType())
            .startTime(createDto.getFrom())
            .endTime(createDto.getTo())
            .build();

        AttendanceTimeConstraint saved = attendanceTimeConstraintDao.save(entity);
        return TimeConstraintMapper.toDto(saved);
    }

    @Override
    public void delete(long timeConstraintId) throws NotFoundException {
        Optional<AttendanceTimeConstraint> atcOpt = attendanceTimeConstraintDao.findById(timeConstraintId);
        if (atcOpt.isEmpty()) {
            throw new NotFoundException("Time constraint not found");
        }
        attendanceTimeConstraintDao.delete(atcOpt.get());
    }
}
