package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.entity.Shift;

public class ShiftMapper {
    public static ShiftDto toShiftDto(Shift shift) {
        return new ShiftDto(String.valueOf(shift.getId()),
            shift.getName(),
            shift.getShortDescription(),
            shift.getLongDescription(),
            shift.getStartTime(),
            shift.getEndTime(),
            ActivityMapper.toActivityDto(shift.getRelatedActivities()),
            null, //Todo: implement when position slots are available
            shift.getLockStatus(),
            null //Todo: implement when trades are available
        );
    }
}
