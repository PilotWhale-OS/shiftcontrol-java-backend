package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.entity.AttendanceTimeConstraint;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TimeConstraintMapper {
    public static TimeConstraintDto toDto(AttendanceTimeConstraint e) {
        return TimeConstraintDto.builder()
            .id(String.valueOf(e.getId()))
            .type(e.getType())
            .from(e.getStartTime())
            .to(e.getEndTime())
            .build();
    }

    public static Collection<TimeConstraintDto> toDto(Collection<AttendanceTimeConstraint> entities) {
        return entities.stream()
            .map(TimeConstraintMapper::toDto)
            .toList();
    }
}
