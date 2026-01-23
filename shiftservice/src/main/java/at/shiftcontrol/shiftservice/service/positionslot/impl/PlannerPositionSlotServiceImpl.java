package at.shiftcontrol.shiftservice.service.positionslot.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.exception.IllegalArgumentException;
import at.shiftcontrol.lib.exception.IllegalStateException;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.ShiftPlanDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.assignment.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentPlannerInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.mapper.AssignmentAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.AssignmentPlannerInfoAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.VolunteerAssemblingMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.positionslot.PlannerPositionSlotService;
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
    private final VolunteerAssemblingMapper volunteerAssemblingMapper;
    private final AssignmentAssemblingMapper assignmentAssemblingMapper;
    private final AssignmentPlannerInfoAssemblingMapper assignmentRequestAssemblingMapper;

    @Override
    public Collection<AssignmentPlannerInfoDto> getSlots(long shiftPlanId, AssignmentFilterDto filterDto) {
        var plan = shiftPlanDao.getById(shiftPlanId);
        securityHelper.assertUserIsPlanner(plan);
        return assignmentRequestAssemblingMapper.toAssignmentPlannerInfoDto(plan.getShifts(), filterDto)
            .stream()
            .filter(assignmentPlannerInfoDto -> !assignmentPlannerInfoDto.getSlots().isEmpty())
            .toList();
    }

    @Override
    public void acceptRequest(long shiftPlanId, long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, userId);
        securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        String routingKey;
        switch (assignment.getStatus()) {
            case ACCEPTED, AUCTION -> throw new IllegalArgumentException("Assignment is not acceptable");
            case AUCTION_REQUEST_FOR_UNASSIGN -> {
                assignmentService.unassignInternal(assignment);
                routingKey = RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_ACCEPTED;
            }
            case REQUEST_FOR_ASSIGNMENT -> {
                if (!eligibilityService.hasCapacity(assignment.getPositionSlot())) {
                    throw new IllegalArgumentException("Slot is already full");
                }
                assignmentService.accept(assignment);
                routingKey = RoutingKeys.POSITIONSLOT_REQUEST_JOIN_ACCEPTED;
            }
            default -> throw new IllegalStateException("Unexpected value: " + assignment.getStatus());
        }

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(routingKey,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", userId)),
            assignment.getPositionSlot(), userId));
    }

    @Override
    public void declineRequest(long shiftPlanId, long positionSlotId, String userId) {
        Assignment assignment = assignmentDao.getAssignmentForPositionSlotAndUser(positionSlotId, userId);
        securityHelper.assertUserIsPlanner(assignment.getPositionSlot());
        String routingKey;
        switch (assignment.getStatus()) {
            case ACCEPTED, AUCTION -> throw new IllegalArgumentException("Assignment is not declineable");
            case AUCTION_REQUEST_FOR_UNASSIGN -> {
                acceptAssignment(assignment);
                routingKey = RoutingKeys.POSITIONSLOT_REQUEST_LEAVE_DECLINED;
            }
            case REQUEST_FOR_ASSIGNMENT -> {
                assignmentDao.delete(assignment);
                routingKey = RoutingKeys.POSITIONSLOT_REQUEST_JOIN_DECLINED;
            }
            default -> throw new IllegalStateException("Unexpected value: " + assignment.getStatus());
        }

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(routingKey,
                Map.of("positionSlotId", String.valueOf(positionSlotId),
                    "volunteerId", userId)),
            assignment.getPositionSlot(), userId));
    }

    @Override
    @Transactional
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

        return volunteerAssemblingMapper.toDto(volunteers);
    }

    private boolean isAssignable(PositionSlot positionSlot, Volunteer volunteer) {
        // check if assignable
        boolean eligible = eligibilityService.isEligibleAndNotSignedUp(positionSlot, volunteer);
        // check for conflicts
        Collection<Assignment> conflicts = eligibilityService.getConflictingAssignmentsExcludingSlot(
            volunteer.getId(), positionSlot, positionSlot.getId());

        return eligible && conflicts.isEmpty();
    }

    @Override
    @Transactional
    public Collection<AssignmentDto> assignUsersToSlot(AssignmentAssignDto assignmentAssignDto) {
        PositionSlot positionSlot = positionSlotDao.getById(
            ConvertUtil.idToLong(assignmentAssignDto.getPositionSlotId()));

        // check access to plan
        securityHelper.assertUserIsPlanner(positionSlot);

        // ignore lock status

        // get all requested volunteers with access to slot
        Collection<Volunteer> volunteers = volunteerDao.findAllByShiftPlanAndVolunteerIds(
            positionSlot.getShift().getShiftPlan().getId(),
            assignmentAssignDto.getVolunteerIds());

        // check if not already signed up, eligible and no conflicts
        volunteers = volunteers.stream().filter(v -> isAssignable(positionSlot, v)).toList();

        // assign volunteers to slot
        Collection<Assignment> assignments = new ArrayList<>(volunteers.size());
        Iterator<Volunteer> iterator = volunteers.stream().iterator();
        while (iterator.hasNext() && eligibilityService.hasCapacity(positionSlot)) {
            assignments.add(
                assignmentService.accept(
                    Assignment.of(positionSlot, iterator.next(), AssignmentStatus.REQUEST_FOR_ASSIGNMENT))
            );
        }

        return assignmentAssemblingMapper.assemble(assignments);
    }

    @Override
    public void unAssignUsersFromSlot(AssignmentAssignDto assignmentAssignDto) {
        PositionSlot positionSlot = positionSlotDao.getById(
            ConvertUtil.idToLong(assignmentAssignDto.getPositionSlotId()));

        // check access to plan
        securityHelper.assertUserIsPlanner(positionSlot);

        // ignore lock status

        Collection<Assignment> assignments =
            assignmentDao.getAssignmentForPositionSlotAndUsers(positionSlot.getId(), assignmentAssignDto.getVolunteerIds());
        assignments.forEach(assignmentService::unassignInternal);
    }

    private void acceptAssignment(Assignment assignment) {
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignmentDao.save(assignment);
    }
}
