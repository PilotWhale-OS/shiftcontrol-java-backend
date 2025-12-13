package at.shiftcontrol.shiftservice.mapper;

import lombok.NonNull;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.type.PositionSignupState;

public class PositionSlotMapper {
    public static PositionSlotDto toDto(@NonNull PositionSlot positionSlot, PositionSignupState positionSignupState) {
        var volunteerUsernames = positionSlot.getAssignments().stream().map(ass -> ass.getAssignedVolunteer().getUsername()).toList();

        return new PositionSlotDto(
            positionSlot.getId(),
            positionSlot.getShift().getId(),
            RoleMapper.toRoleDto(positionSlot.getRole()),
            volunteerUsernames,
            positionSlot.getDesiredVolunteerCount(),
            positionSignupState);
    }
}
