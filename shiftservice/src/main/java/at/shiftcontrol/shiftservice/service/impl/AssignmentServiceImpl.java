package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.shiftservice.mapper.AssignmentMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
    private final RewardPointsService rewardPointsService;
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;
    private final ApplicationEventPublisher publisher;

    @Override
    public Assignment reassign(Assignment oldAssignment, Volunteer newVolunteer) {
        // Create new assignment with new PK
        Assignment newAssignment = AssignmentMapper.shallowCopy(oldAssignment);
        newAssignment.setId(new AssignmentId(oldAssignment.getPositionSlot().getId(), newVolunteer.getId()));
        newAssignment.setStatus(AssignmentStatus.ACCEPTED);
        newAssignment.setAssignedVolunteer(newVolunteer);

        // Update PositionSlot assignments
        PositionSlot slot = newAssignment.getPositionSlot();
        if (slot.getAssignments() != null) {
            slot.getAssignments().remove(oldAssignment);
            slot.getAssignments().add(newAssignment);
        }

        assignmentDao.save(newAssignment);
        // Reassign dependent switch requests
        newAssignment.getIncomingSwitchRequests()
            .forEach(req -> req.setRequestedAssignment(newAssignment));
        newAssignment.getOutgoingSwitchRequests()
            .forEach(req -> req.setOfferingAssignment(newAssignment));
        assignmentSwitchRequestDao.saveAll(oldAssignment.getIncomingSwitchRequests());
        assignmentSwitchRequestDao.saveAll(oldAssignment.getOutgoingSwitchRequests());
        // Delete old assignment
        assignmentDao.delete(oldAssignment);
        return newAssignment;
    }

    @Override
    @Transactional
    public void unassignAllAuctions(ShiftPlan shiftPlan) {
        Collection<Assignment> auctions = assignmentDao.findAuctionsByShiftPlanId(shiftPlan.getId());

        auctions.forEach(auction -> {
            rewardPointsService.onAssignmentRemoved(
                auction
            );

            // publish event
            publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                    Map.of("positionSlotId", String.valueOf((auction.getPositionSlot().getId())),
                        "volunteerId", auction.getAssignedVolunteer().getId())),
                auction.getPositionSlot(), auction.getAssignedVolunteer().getId()));
        });

        assignmentDao.deleteAll(auctions);
    }
}
