package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import lombok.NonNull;

public class PositionSlotMapper {
    public static PositionSlotDto toDto(@NonNull PositionSlot positionSlot, PositionSignupState positionSignupState) {
        var volunteers = positionSlot.getAssignments().stream().map(Assignment::getAssignedVolunteer).toList();

        return new PositionSlotDto(
            positionSlot.getId(),
            positionSlot.getShift().getId(),
            RoleMapper.toRoleDto(positionSlot.getRole()),
            VolunteerMapper.toDto(volunteers),
            positionSlot.getDesiredVolunteerCount(),
            -1, // TODO: Add rewardPoints in entity and add correct mapping
            positionSignupState);
    }
}
