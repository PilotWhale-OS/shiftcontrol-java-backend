package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.NonNull;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;

public class AssignmentMapper {
    public static AssignmentDto toDto(@NonNull Assignment assignment) {
        return new AssignmentDto(
            assignment.getPositionSlot().getId(),
            VolunteerMapper.toDto(assignment.getAssignedVolunteer()),
            assignment.getStatus());
    }

    public static Collection<AssignmentDto> toDto(@NonNull Collection<Assignment> assignments) {
        return assignments.stream().map(AssignmentMapper::toDto).toList();
    }
}
