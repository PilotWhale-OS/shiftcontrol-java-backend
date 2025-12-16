package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import lombok.NonNull;

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
}
