package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.shiftservice.dto.assignment.PositionSlotContextDto;

import lombok.NonNull;

public class PositionSlotContextMapper
{
    public static PositionSlotContextDto toDto(@NonNull PositionSlot positionSlot) {
        return new PositionSlotContextDto(
            String.valueOf(positionSlot.getId()),
            positionSlot.getName(),
            positionSlot.getDescription(),
            positionSlot.isSkipAutoAssignment(),
            positionSlot.getDesiredVolunteerCount(),
            positionSlot.getRole() == null ? null :RoleMapper.toRoleDto(positionSlot.getRole())
        );
    }
}
