package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import at.shiftcontrol.shiftservice.dto.plannerdashboard.SlotAssignmentsDto;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentPlannerInfoDto;

@RequiredArgsConstructor
@Service
public class AssignmentPlannerInfoAssemblingMapper {
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;

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
        Collection<SlotAssignmentsDto> requests =
            safeSlots(shift.getSlots()).stream()
                .map(slot -> toSlotAssignmentsDto(slot, filterDto))
                .filter(slotAssignmentsDto -> !slotAssignmentsDto.getAssignments().isEmpty())
                .collect(Collectors.toList());

        return AssignmentPlannerInfoDto.builder()
            .shiftId(shift.getId())
            .shiftName(shift.getName())
            .slots(requests)
            .build();
    }

    private SlotAssignmentsDto toSlotAssignmentsDto(PositionSlot slot, AssignmentFilterDto filterDto) {
        Collection<AssignmentDto> assignmentDtos = safeAssignments(slot.getAssignments()).stream()
            .map(assignment -> toAssignmentDto(slot, assignment))
            .filter(a -> filterDto == null
                || filterDto.getStatuses() == null
                || filterDto.getStatuses().contains(a.getStatus()))
            .collect(Collectors.toList());

        return SlotAssignmentsDto.builder()
            .positionSlotName(String.valueOf(slot.getName()))
            .assignments(assignmentDtos)
            .build();
    }

    private AssignmentDto toAssignmentDto(PositionSlot slot, Assignment assignment) {
        return AssignmentDto.builder()
            .positionSlotId(String.valueOf(slot.getId()))
            .assignedVolunteer(volunteerAssemblingMapper.toDto(assignment.getAssignedVolunteer()))
            .status(assignment.getStatus())
            .acceptedRewardPoints(assignment.getAcceptedRewardPoints())
            .build();
    }

    private static Collection<PositionSlot> safeSlots(Collection<PositionSlot> slots) {
        return slots == null ? List.of() : slots;
    }

    private static Collection<Assignment> safeAssignments(Collection<Assignment> assignments) {
        return assignments == null ? List.of() : assignments;
    }
}

