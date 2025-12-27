package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
