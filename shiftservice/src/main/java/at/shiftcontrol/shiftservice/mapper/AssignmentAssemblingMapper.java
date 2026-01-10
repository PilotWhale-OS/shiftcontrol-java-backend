package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@RequiredArgsConstructor
@Service
public class AssignmentAssemblingMapper {
    public static final EnumSet<AssignmentStatus> ACTIVE_AUCTION_STATES =
        EnumSet.of(AssignmentStatus.AUCTION, AssignmentStatus.AUCTION_REQUEST_FOR_UNASSIGN);

    private final VolunteerAssemblingMapper volunteerAssemblingMapper;

    public AssignmentDto toDto(@NonNull Assignment assignment) {
        return new AssignmentDto(
            String.valueOf(assignment.getPositionSlot().getId()),
            volunteerAssemblingMapper.toDto(assignment.getAssignedVolunteer()),
            assignment.getStatus(),
            assignment.getAcceptedRewardPoints());
    }

    public Collection<AssignmentDto> toDto(Collection<Assignment> assignments) {
        if (assignments == null) {
            return List.of();
        }
        return assignments.stream().map(this::toDto).toList();
    }

    public static AssignmentId toEntityId(@NonNull AssignmentDto assignmentDto) {
        return new AssignmentId(
            ConvertUtil.idToLong(assignmentDto.getPositionSlotId()),
            assignmentDto.getAssignedVolunteer().getId()
        );
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
