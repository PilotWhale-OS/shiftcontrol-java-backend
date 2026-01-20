package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import at.shiftcontrol.shiftservice.dto.assignment.AssignmentContextDto;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentPlannerInfoDto;

@RequiredArgsConstructor
@Service
public class AssignmentPlannerInfoAssemblingMapper {
    private final AssignmentContextAssemblingMapper assignmentContextAssemblingMapper;

    public Collection<AssignmentPlannerInfoDto> toAssignmentPlannerInfoDto(Collection<Shift> shifts, AssignmentFilterDto filterDto) {
        if (shifts == null || shifts.isEmpty()) {
            return List.of();
        }

        return shifts.stream()
            .filter(Objects::nonNull)
            .map(shift -> toAssignmentPlannerInfoDto(shift, filterDto))
            .collect(Collectors.toList());
    }

    private AssignmentPlannerInfoDto toAssignmentPlannerInfoDto(Shift shift, AssignmentFilterDto filterDto) {
        Collection<AssignmentContextDto> requests =
            safeSlots(shift.getSlots()).stream()
                .map(slot -> toSlotAssignmentsDto(slot, filterDto))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return AssignmentPlannerInfoDto.builder()
            .shiftId(shift.getId())
            .slots(requests)
            .build();
    }

    private Collection<AssignmentContextDto> toSlotAssignmentsDto(PositionSlot slot, AssignmentFilterDto filterDto) {
        return safeAssignments(slot.getAssignments()).stream()
            .map(assignmentContextAssemblingMapper::toDto)
            .filter(a -> filterDto == null
                || filterDto.getStatuses() == null
                || filterDto.getStatuses().contains(a.getAssignment().getStatus()))
            .collect(Collectors.toList());
    }

    private static Collection<PositionSlot> safeSlots(Collection<PositionSlot> slots) {
        return slots == null ? List.of() : slots;
    }

    private static Collection<Assignment> safeAssignments(Collection<Assignment> assignments) {
        return assignments == null ? List.of() : assignments;
    }
}

