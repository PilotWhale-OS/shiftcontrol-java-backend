package at.shiftcontrol.shiftservice.assembler;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.mapper.ShiftMapper;

@RequiredArgsConstructor
@Service
public class ShiftDtoAssembler {

    private final PositionSlotDtoAssembler positionSlotDtoAssembler;

    public ShiftDto assemble(@NonNull Shift shift) {
        return ShiftMapper.toDto(shift, positionSlotDtoAssembler.assemble(shift.getSlots()));
    }

    public Collection<ShiftDto> assemble(@NonNull Collection<Shift> shifts) {
        return shifts.stream().map(this::assemble).toList();
    }
}
