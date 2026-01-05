package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.EnumSet;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import lombok.NonNull;

public class AssignmentMapper {
    public static final EnumSet<AssignmentStatus> ACTIVE_AUCTION_STATES =
        EnumSet.of(AssignmentStatus.AUCTION, AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN);

    public static AssignmentDto toDto(@NonNull Assignment assignment) {
        return new AssignmentDto(
            String.valueOf(assignment.getPositionSlot().getId()),
            VolunteerMapper.toDto(assignment.getAssignedVolunteer()),
            assignment.getStatus(),
            assignment.getAcceptedRewardPoints());
    }

    public static Collection<AssignmentDto> toDto(@NonNull Collection<Assignment> assignments) {
        return assignments.stream().map(AssignmentMapper::toDto).toList();
    }

    public static AssignmentId toEntityId(@NonNull AssignmentDto assignmentDto) {
        return new AssignmentId(
            ConvertUtil.idToLong(assignmentDto.getPositionSlotId()),
            assignmentDto.getAssignedVolunteer().getId()
        );
    }

    public static Collection<AssignmentDto> toAuctionDto(@NonNull Collection<Assignment> assignments) {
        return assignments.stream().filter(
                a -> ACTIVE_AUCTION_STATES.contains(a.getStatus()))
            .map(AssignmentMapper::toDto).toList();
    }

    public static Assignment shallowCopy(@NonNull Assignment oldAssignment) {
        return Assignment.builder()
            .id(new AssignmentId(
                oldAssignment.getPositionSlot().getId(),
                oldAssignment.getAssignedVolunteer().getId()))
            .status(oldAssignment.getStatus())
            .assignedVolunteer(oldAssignment.getAssignedVolunteer())
            .positionSlot(oldAssignment.getPositionSlot())
            .incomingSwitchRequests(oldAssignment.getIncomingSwitchRequests())
            .outgoingSwitchRequests(oldAssignment.getOutgoingSwitchRequests())
            .build();
    }
}
