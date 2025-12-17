package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.type.PositionSignupState;
import lombok.NonNull;

// use PositionSlotDtoAssembler to convert entities and collections to dtos and compute their PositionSignupState
public class PositionSlotMapper {
    public static PositionSlotDto toDto(@NonNull PositionSlot positionSlot, @NonNull PositionSignupState positionSignupState) {
        var volunteers = positionSlot.getAssignments().stream().map(Assignment::getAssignedVolunteer).toList();

        return new PositionSlotDto(
            String.valueOf(positionSlot.getId()),
            String.valueOf(positionSlot.getShift().getId()),
            RoleMapper.toRoleDto(positionSlot.getRole()),
            VolunteerMapper.toDto(volunteers),
            positionSlot.getDesiredVolunteerCount(),
            positionSlot.getRewardPoints(),
            positionSignupState);
    }
}
