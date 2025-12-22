package at.shiftcontrol.shiftservice.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

@RequiredArgsConstructor
@Service
public class ShiftAssemblingMapper {
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;

    public ShiftDto assemble(@NonNull Shift shift) {
        return toDto(shift, positionSlotAssemblingMapper.assemble(shift.getSlots()));
    }

    public Collection<ShiftDto> assemble(@NonNull Collection<Shift> shifts) {
        return shifts.stream().map(this::assemble).toList();
    }

    public static ShiftDto toShiftDto(@NonNull Shift shift, Function<PositionSlot, PositionSignupState> positionSlotSignupStateResolver) {
        var slots = new ArrayList<PositionSlotDto>();
        for (var slot : shift.getSlots()) {
            var signupState = positionSlotSignupStateResolver.apply(slot);
            slots.add(PositionSlotAssemblingMapper.toDto(slot, signupState, List.of()));
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
            LocationMapper.toLocationDto(shift.getLocation())
        );
    }

    public static List<ShiftDto> toShiftDto(Collection<Shift> shifts, Function<PositionSlot, PositionSignupState> positionSlotSignupStateResolver) {
        return shifts.stream()
            .map(shift -> toShiftDto(shift, positionSlotSignupStateResolver))
            .toList();
    }

    public static ShiftDto toDto(@NonNull Shift shift, Collection<PositionSlotDto> positionSlots) {
        return new ShiftDto(
            String.valueOf(shift.getId()),
            shift.getName(),
            shift.getShortDescription(),
            shift.getLongDescription(),
            shift.getStartTime(),
            shift.getEndTime(),
            ActivityMapper.toActivityDto(shift.getRelatedActivities()),
            positionSlots,
            shift.getLockStatus(),
            LocationMapper.toLocationDto(shift.getLocation())
        );
    }
}
