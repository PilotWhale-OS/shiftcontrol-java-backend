package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.entity.Shift;

public class ShiftMapper {
    public static ShiftDto toDto(@NonNull Shift shift, Collection<PositionSlotDto> positionSlots) {
        return new ShiftDto(String.valueOf(shift.getId()),
            shift.getName(),
            shift.getShortDescription(),
            shift.getLongDescription(),
            shift.getStartTime(),
            shift.getEndTime(),
            ActivityMapper.toActivityDto(shift.getRelatedActivities()),
            positionSlots,
            shift.getLockStatus(),
            null //Todo: implement when trades are available
        );
    }
}
