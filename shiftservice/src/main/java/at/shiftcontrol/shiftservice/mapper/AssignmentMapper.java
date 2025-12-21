package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.EnumSet;

import lombok.NonNull;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

public class AssignmentMapper {
    public static AssignmentDto toDto(@NonNull Assignment assignment) {
        return new AssignmentDto(
            String.valueOf(assignment.getPositionSlot().getId()),
            VolunteerMapper.toDto(assignment.getAssignedVolunteer()),
            assignment.getStatus());
    }

    public static Collection<AssignmentDto> toDto(@NonNull Collection<Assignment> assignments) {
        return assignments.stream().map(AssignmentMapper::toDto).toList();
    }

    public static AssignmentId toEntityId(@NonNull AssignmentDto assignmentDto) {
        return new AssignmentId(
            ConvertUtil.idToLong(assignmentDto.getPositionSlotId()),
            ConvertUtil.idToLong(assignmentDto.getAssignedVolunteer().getId())
        );
    }

    public static Collection<AssignmentDto> toAuctionDto(@NonNull Collection<Assignment> assignments) {
        return assignments.stream().filter(
            a -> EnumSet.of(AssignmentStatus.AUCTION, AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN).contains(a.getStatus()))
            .map(AssignmentMapper::toDto).toList();
    }
}
