package at.shiftcontrol.shiftservice.assembler;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.PositionSlotMapper;
import at.shiftcontrol.shiftservice.service.EligibilityService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class PositionSlotDtoAssembler {

    private final EligibilityService eligibilityService;

    public PositionSlotDto assemble(@NonNull PositionSlot positionSlot) {
        return PositionSlotMapper.toDto(positionSlot, eligibilityService.getSignupStateForPositionSlot(
            positionSlot, Volunteer.builder().id(1L).build())); // TODO get current User
    }

    public Collection<PositionSlotDto> assemble(@NonNull Collection<PositionSlot> positionSlots) {
        return positionSlots.stream().map(this::assemble).toList();
    }
}
