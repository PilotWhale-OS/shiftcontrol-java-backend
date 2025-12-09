package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
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

    public PositionSlotDto toPositionSlotDto(PositionSlot positionSlot) {
        return new PositionSlotDto(
            String.valueOf(positionSlot.getId()),
            String.valueOf(positionSlot.getShift().getId()),
            RoleMapper.toRoleDto(positionSlot.getRole()),
            null, //Todo
            positionSlot.getDesiredVolunteerCount(),
            null); //Todo We probably need a helper class or something for this business logic,
    }
}
