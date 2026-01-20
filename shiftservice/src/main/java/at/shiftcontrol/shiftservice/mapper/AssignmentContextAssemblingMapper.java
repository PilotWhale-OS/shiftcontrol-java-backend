package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentContextDto;

@RequiredArgsConstructor
@Service
public class AssignmentContextAssemblingMapper {
    private final AssignmentAssemblingMapper assignmentAssemblingMapper;

    public AssignmentContextDto toDto(@NonNull Assignment assignment) {
        return new AssignmentContextDto(
            assignmentAssemblingMapper.assemble(assignment),
            ShiftContextMapper.toDto(assignment.getPositionSlot().getShift()),
            PositionSlotContextMapper.toDto(assignment.getPositionSlot())
        );
    }

    public Collection<AssignmentContextDto> toDto(Collection<Assignment> assignments) {
        if (assignments == null) {
            return List.of();
        }
        return assignments.stream().map(this::toDto).toList();
    }
}
