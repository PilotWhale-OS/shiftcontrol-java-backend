package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;

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
}
