package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.type.TimeConstraintType;
import at.shiftcontrol.shiftservice.dao.TimeConstraintDao;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentContextDto;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@RequiredArgsConstructor
@Service
public class AssignmentAssemblingMapper {
    private final UserDirectoryService userDirectoryService;
    private final PositionSlotContextAssemblingMapper positionSlotContextAssemblingMapper;
    private final TimeConstraintDao timeConstraintDao;

    public AssignmentDto assemble(@NonNull Assignment assignment) {
        return assemble(assignment, userDirectoryService.getUserById(assignment.getAssignedVolunteer().getId()));
    }

    public Collection<AssignmentDto> assemble(Collection<Assignment> assignments) {
        if (assignments == null) {
            return List.of();
        }

        Map<String, DirectoryUser> usersById = userDirectoryService.getUserByIds(
            assignments.stream()
                .map(Assignment::getAssignedVolunteer)
                .map(Volunteer::getId)
                .distinct()
                .toList()
        ).stream().collect(Collectors.toMap(DirectoryUser::id, Function.identity()));

        return assignments.stream()
            .map(assignment -> assemble(
                assignment,
                usersById.getOrDefault(
                    assignment.getAssignedVolunteer().getId(),
                    fallbackDirectoryUser(assignment.getAssignedVolunteer().getId())
                )
            ))
            .toList();
    }

    private AssignmentDto assemble(@NonNull Assignment assignment, DirectoryUser user) {
        var hasEmergencyConstraint = timeConstraintDao.findByAssignmentIdAndType(assignment.getId(), TimeConstraintType.EMERGENCY)
            .isPresent();

        return new AssignmentDto(
            String.valueOf(assignment.getId()),
            String.valueOf(assignment.getPositionSlot().getId()),
            VolunteerAssemblingMapper.toDtoFromUser(user),
            assignment.getStatus(),
            assignment.getAcceptedRewardPoints(),
            hasEmergencyConstraint);
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

    private DirectoryUser fallbackDirectoryUser(String userId) {
        return new DirectoryUser(userId, userId, "", "", "", at.shiftcontrol.shiftservice.auth.UserType.ASSIGNED);
    }
}
