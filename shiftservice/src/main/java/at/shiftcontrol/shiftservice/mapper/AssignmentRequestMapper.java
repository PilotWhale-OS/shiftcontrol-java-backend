package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentRequestDto;

@RequiredArgsConstructor
@Service
public class AssignmentRequestMapper {
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;

    public Collection<AssignmentRequestDto> toAssignmentRequestDto(Collection<Shift> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            return List.of();
        }

        return shifts.stream()
            .filter(Objects::nonNull)
            .map(this::toAssignmentRequestDto)
            .collect(Collectors.toList());
    }

    private AssignmentRequestDto toAssignmentRequestDto(Shift shift) {
        Collection<AssignmentDto> requests =
            safeSlots(shift.getSlots()).stream()
                .flatMap(slot -> safeAssignments(slot.getAssignments()).stream()
                    .map(assignment -> toAssignmentDto(slot, assignment))
                )
                .collect(Collectors.toList());

        return AssignmentRequestDto.builder()
            .shiftId(shift.getId())
            .shiftName(shift.getName())
            .requests(requests)
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

