package at.shiftcontrol.shiftservice.service.positionslot.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.AssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentRequestDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.shiftservice.mapper.AssignmentRequestMapper;
import at.shiftcontrol.shiftservice.mapper.VolunteerMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.positionslot.PlannerPositionSlotService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class PlannerPositionSlotServiceImpl implements PlannerPositionSlotService {
    private final SecurityHelper securityHelper;
    private final AssignmentService assignmentService;
    private final ShiftPlanDao shiftPlanDao;
    private final AssignmentDao assignmentDao;
    private final PositionSlotDao positionSlotDao;
    private final VolunteerDao volunteerDao;
    private final ApplicationEventPublisher publisher;
    private final EligibilityService eligibilityService;

    @Override
    public Collection<AssignmentRequestDto> getSlots(long shiftPlanId, AssignmentFilterDto filterDto) {
        var plan = shiftPlanDao.getById(shiftPlanId);
        securityHelper.assertUserIsPlanner(plan);
        return AssignmentRequestMapper.toAssignmentRequestDto(plan.getShifts());
    }

    @Override
    public void acceptRequest(long shiftPlanId, long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, userId);
        securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        switch (assignment.getStatus()) {
            case ACCEPTED, AUCTION -> throw new IllegalArgumentException("Assignment is not acceptable");
            case AUCTION_REQUEST_FOR_UNASSIGN -> assignmentService.unassign(assignment);
            case REQUEST_FOR_ASSIGNMENT -> assignmentService.accept(assignment);
            default -> throw new IllegalStateException("Unexpected value: " + assignment.getStatus());
        }

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_ACCEPTED,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", userId)),
            assignment.getPositionSlot(), userId));
    }

    @Override
    public void declineRequest(long shiftPlanId, long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, userId);
        securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        switch (assignment.getStatus()) {
            case ACCEPTED, AUCTION -> throw new IllegalArgumentException("Assignment is not declineable");
            case AUCTION_REQUEST_FOR_UNASSIGN -> acceptAssignment(assignment);
            case REQUEST_FOR_ASSIGNMENT -> assignmentDao.delete(assignment);
            default -> throw new IllegalStateException("Unexpected value: " + assignment.getStatus());
        }

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_DECLINED,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", userId)),
            assignment.getPositionSlot(), userId));
    }

    @Override
    public Collection<VolunteerDto> getAssignableUsers(String positionSlotId) {
        PositionSlot positionSlot = positionSlotDao.getById(
            ConvertUtil.idToLong(positionSlotId));

        // check access to plan
        securityHelper.assertUserIsPlanner(positionSlot);

        // get all volunteers with access to slot
        Collection<Volunteer> volunteers =
            volunteerDao.findAllByShiftPlan(positionSlot.getShift().getShiftPlan().getId());

        // check if not already signed up, eligible and no conflicts
        volunteers = volunteers.stream().filter(v -> isAssignable(positionSlot, v)).toList();

        return VolunteerMapper.toDto(volunteers);
    }

    private boolean isAssignable(PositionSlot positionSlot, Volunteer volunteer) {
        // check if assignable
        boolean eligible = eligibilityService.isEligibleAndNotSignedUp(positionSlot, volunteer);
        // check for conflicts
        Collection<Assignment> conflicts = eligibilityService.getConflictingAssignments(
            volunteer.getId(), positionSlot);

        return eligible && conflicts.isEmpty();
    }

    @Override
    public Collection<AssignmentDto> assignUsersToSlot(AssignmentAssignDto assignmentAssignDto) {
        // TODO implement
        PositionSlot positionSlot = positionSlotDao.getById(
            ConvertUtil.idToLong(assignmentAssignDto.getPositionSlotId()));

        // check access to plan
        securityHelper.assertUserIsPlanner(positionSlot);

        // ignore lock status

        // volunteers have to:
        // have access to slot
        // be eligible (handle already signed up)
        // have no conflicts


        return List.of();
    }

    private void acceptAssignment(Assignment assignment) {
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignmentDao.save(assignment);
    }
}
