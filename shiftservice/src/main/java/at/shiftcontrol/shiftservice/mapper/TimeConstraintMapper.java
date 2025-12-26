package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.Attendance;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TimeConstraintMapper {
    public static TimeConstraintDto toDto(AttendanceTimeConstraint e) {
        return new TimeConstraintDto(
                String.valueOf(e.getId()),
                e.getType(),
                e.getStartTime(),
                e.getEndTime()
            );
    }

    public static Collection<TimeConstraintDto> toDto(Collection<AttendanceTimeConstraint> entities) {
        return entities.stream()
            .map(TimeConstraintMapper::toDto)
            .toList();
    }

    // Create entity from create DTO and resolved Attendance
    public static AttendanceTimeConstraint fromCreateDto(TimeConstraintCreateDto dto, Attendance attendance) {
        return AttendanceTimeConstraint.builder()
            .attendance(attendance)
            .type(dto.getType())
            .startTime(dto.getFrom())
            .endTime(dto.getTo())
            .build();
    }
}
