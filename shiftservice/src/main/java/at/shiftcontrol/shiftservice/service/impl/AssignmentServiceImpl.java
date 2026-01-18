package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.AssignmentEvent;
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;
import at.shiftcontrol.shiftservice.mapper.AssignmentAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.TradeMapper;
import at.shiftcontrol.shiftservice.service.AssignmentService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
    private final RewardPointsService rewardPointsService;
    private final AssignmentDao assignmentDao;
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;
    private final ApplicationEventPublisher publisher;

    @Override
    public Assignment claimAuction(Assignment auction, Volunteer newVolunteer, PositionSlotRequestDto requestDto) {
        String oldVolunteerId = auction.getAssignedVolunteer().getId();
        Assignment oldAuction = AssignmentAssemblingMapper.shallowCopy(auction);
        // execute auction
        auction = reassign(auction, newVolunteer);

        // update reward points
        rewardPointsService.onAssignmentReassignedAuction(oldAuction, auction, requestDto.getAcceptedRewardPointsConfigHash());

        publisher.publishEvent(AssignmentEvent.of(RoutingKeys.format(RoutingKeys.AUCTION_CLAIMED, Map.of(
            "positionSlotId", String.valueOf(oldAuction.getPositionSlot().getId()),
            "oldVolunteerId", oldVolunteerId)), auction
        ));

        return auction;
    }

    @Override
    public AssignmentSwitchRequest executeTrade(AssignmentSwitchRequest trade) {
        // delete inverse trade if exists
        assignmentSwitchRequestDao.findInverseTrade(trade).ifPresent(assignmentSwitchRequestDao::delete);
        // cancel all trades for involved assignments
        cancelOtherTrades(trade);

        // update fields and id of assignments
        AssignmentSwitchRequest oldTrade = TradeMapper.shallowCopy(trade);
        reassign(trade.getOfferingAssignment(), oldTrade.getRequestedAssignment().getAssignedVolunteer());
        reassign(trade.getRequestedAssignment(), oldTrade.getOfferingAssignment().getAssignedVolunteer());

        rewardPointsService.onAssignmentReassignedTrade(trade.getOfferingAssignment(), trade.getRequestedAssignment());

        publisher.publishEvent(AssignmentSwitchEvent.of(oldTrade.getRequestedAssignment(), oldTrade.getOfferingAssignment()));

        return trade;
    }

    private void cancelOtherTrades(AssignmentSwitchRequest trade) {
        // this trade does not need to be excluded because it will be set to ACCEPTED in the next step
        assignmentSwitchRequestDao.cancelTradesForAssignment(
            trade.getRequestedAssignment().getPositionSlot().getId(), trade.getRequestedAssignment().getAssignedVolunteer().getId());
        assignmentSwitchRequestDao.cancelTradesForAssignment(
            trade.getOfferingAssignment().getPositionSlot().getId(), trade.getOfferingAssignment().getAssignedVolunteer().getId());
    }

    /**
     * reassigns the assignment to the given volunteer.
     * handles all dependencies of the assignment, since reassigning results in a new primary key
     * deletes the old assignment and persists a new one in the process
     *
     * @param oldAssignment the assignment containing the old assigned volunteer
     * @param newVolunteer volunteer to replace the old volunteer
     * @return new assignment where the given volunteer is assigned
     */
    private Assignment reassign(Assignment oldAssignment, Volunteer newVolunteer) {
        oldAssignment.setAssignedVolunteer(newVolunteer);
        oldAssignment.setStatus(AssignmentStatus.ACCEPTED);
        oldAssignment = assignmentDao.save(oldAssignment);
        return oldAssignment;
    }

    @Override
    public Assignment accept(Assignment assignment) {
        if (assignment.getStatus() != AssignmentStatus.REQUEST_FOR_ASSIGNMENT) {
            throw new IllegalArgumentException("Assignment must be a request for assignment");
        }

        // update reward points
        rewardPointsService.onAssignmentAccepted(assignment);

        assignment.setStatus(AssignmentStatus.ACCEPTED);

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                Map.of("positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                    "volunteerId", assignment.getAssignedVolunteer().getId())),
            assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));

        return assignmentDao.save(assignment);
    }

    @Override
    public Assignment assign(@NonNull PositionSlot positionSlot, @NonNull Volunteer volunteer, @NonNull PositionSlotRequestDto requestDto) {
        // create assignment
        Assignment assignment = Assignment.of(positionSlot, volunteer, AssignmentStatus.ACCEPTED);

        // update reward points
        rewardPointsService.onAssignmentCreated(
            assignment,
            requestDto.getAcceptedRewardPointsConfigHash());

        // close trades where this slot was offered to current user
        assignmentSwitchRequestDao.cancelTradesForOfferedPositionAndRequestedUser(positionSlot.getId(), volunteer.getId());

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_JOINED,
                Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                    "volunteerId", volunteer.getId())),
            positionSlot, volunteer.getId()));

        return assignmentDao.save(assignment);
    }

    @Override
    public void unassign(Assignment assignment) {
        // update reward points
        rewardPointsService.onAssignmentRemoved(
            assignment
        );

        // leave
        assignmentDao.delete(assignment);

        publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_LEFT,
                Map.of("positionSlotId", String.valueOf(assignment.getPositionSlot().getId()),
                    "volunteerId", assignment.getAssignedVolunteer().getId())),
            assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
    }

    @Override
    @Transactional
    public void unassignAllAuctions(ShiftPlan shiftPlan) {
        Collection<Assignment> auctions = assignmentDao.findAuctionsByShiftPlanId(shiftPlan.getId());

        auctions.forEach(auction -> {
            // update reward points
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

    @Override
    @Transactional
    public void declineAllSignupRequests(ShiftPlan shiftPlan) {
        Collection<Assignment> requests = assignmentDao.findSignupRequestsByShiftPlanId(shiftPlan.getId());

        requests.forEach(request -> {
            // publish event
            publisher.publishEvent(PositionSlotVolunteerEvent.of(RoutingKeys.format(RoutingKeys.POSITIONSLOT_REQUEST_JOIN_DECLINED,
                    Map.of("positionSlotId", String.valueOf((request.getPositionSlot().getId())),
                        "volunteerId", request.getAssignedVolunteer().getId())),
                request.getPositionSlot(), request.getAssignedVolunteer().getId()));
        });

        assignmentDao.deleteAll(requests);
    }
}
