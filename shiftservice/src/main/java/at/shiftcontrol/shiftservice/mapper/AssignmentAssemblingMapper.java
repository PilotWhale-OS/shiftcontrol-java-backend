package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.type.TimeConstraintType;
import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentContextDto;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentDto;

@RequiredArgsConstructor
@Service
public class AssignmentAssemblingMapper {
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;
    private final PositionSlotContextAssemblingMapper positionSlotContextAssemblingMapper;
    private final TimeConstraintDao timeConstraintDao;

    public AssignmentDto assemble(@NonNull Assignment assignment) {
        var hasEmergencyConstraint = timeConstraintDao.findByAssignmentIdAndType(assignment.getId(), TimeConstraintType.EMERGENCY)
            .isPresent();

        return new AssignmentDto(
            String.valueOf(assignment.getId()),
            String.valueOf(assignment.getPositionSlot().getId()),
            volunteerAssemblingMapper.toDto(assignment.getAssignedVolunteer()),
            assignment.getStatus(),
            assignment.getAcceptedRewardPoints(),
            hasEmergencyConstraint);
    }

    public Collection<AssignmentDto> assemble(Collection<Assignment> assignments) {
        if (assignments == null) {
            return List.of();
        }
        return assignments.stream().map(this::assemble).toList();
    }

    public static Assignment shallowCopy(@NonNull Assignment oldAssignment) {
        return Assignment.builder()
            .positionSlot(oldAssignment.getPositionSlot())
            .assignedVolunteer(oldAssignment.getAssignedVolunteer())
            .status(oldAssignment.getStatus())
            .outgoingSwitchRequests(oldAssignment.getOutgoingSwitchRequests())
            .incomingSwitchRequests(oldAssignment.getIncomingSwitchRequests())
            .acceptedRewardPoints(oldAssignment.getAcceptedRewardPoints())
            .build();
    }

    public AssignmentContextDto toContextDto(@NonNull Assignment assignment) {
        return new AssignmentContextDto(
            assemble(assignment),
            ShiftAssemblingMapper.toContextDto(assignment.getPositionSlot().getShift()),
            positionSlotContextAssemblingMapper.toContextDto(assignment.getPositionSlot())
        );
    }

    public Collection<AssignmentContextDto> toContextDto(Collection<Assignment> assignments) {
        if (assignments == null) {
            return List.of();
        }
        return assignments.stream().map(this::toContextDto).toList();
    }
}
