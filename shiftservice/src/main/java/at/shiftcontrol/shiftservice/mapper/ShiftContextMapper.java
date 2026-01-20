package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.shiftservice.dto.assignment.ShiftContextDto;

public class ShiftContextMapper {
    public static ShiftContextDto toDto(Shift shift){
        return new ShiftContextDto(
            String.valueOf(shift.getId()),
            shift.getName(),
            shift.getShortDescription(),
            shift.getLongDescription(),
            shift.getStartTime(),
            shift.getEndTime(),
            LocationMapper.toLocationDto(shift.getLocation()),
            shift.getBonusRewardPoints()
        );
    }
}
