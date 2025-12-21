package at.shiftcontrol.shiftservice.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

public class ShiftMapper {
    public static ShiftDto toShiftDto(Shift shift, Function<PositionSlot, PositionSignupState> positionSlotSignupStateResolver) {
        var slots = new ArrayList<PositionSlotDto>();
        for (var slot : shift.getSlots()) {
            var signupState = positionSlotSignupStateResolver.apply(slot);
            slots.add(PositionSlotMapper.toDto(slot, signupState));
        }

        return new ShiftDto(String.valueOf(shift.getId()),
            shift.getName(),
            shift.getShortDescription(),
            shift.getLongDescription(),
            shift.getStartTime(),
            shift.getEndTime(),
            ActivityMapper.toActivityDto(shift.getRelatedActivities()),
            slots,
            shift.getLockStatus(),
            null, //Todo: implement when trades are available,
            LocationMapper.toLocationDto(shift.getLocation())
        );
    }

    public static List<ShiftDto> toShiftDto(Collection<Shift> shifts, Function<PositionSlot, PositionSignupState> positionSlotSignupStateResolver) {
        return shifts.stream()
            .map(shift -> toShiftDto(shift, positionSlotSignupStateResolver))
            .toList();
    }
}
