package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

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
import at.shiftcontrol.lib.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.lib.event.events.ClaimedAuctionEvent;
import at.shiftcontrol.lib.event.events.PositionSlotVolunteerEvent;
import at.shiftcontrol.lib.exception.IllegalArgumentException;
import at.shiftcontrol.lib.exception.IllegalStateException;
import at.shiftcontrol.lib.type.AssignmentStatus;
import at.shiftcontrol.lib.type.TradeStatus;
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
    public @org.jspecify.annotations.NonNull Assignment claimAuction(@org.jspecify.annotations.NonNull Assignment auction, @org.jspecify.annotations.NonNull Volunteer newVolunteer, @org.jspecify.annotations.NonNull PositionSlotRequestDto requestDto) {
        String oldVolunteerId = auction.getAssignedVolunteer().getId();
        Assignment oldAuction = AssignmentAssemblingMapper.shallowCopy(auction);
        // execute auction
        auction = reassign(auction, newVolunteer);

        // update reward points
        rewardPointsService.onAssignmentReassignedAuction(oldAuction, auction, requestDto.getAcceptedRewardPointsConfigHash());

        publisher.publishEvent(ClaimedAuctionEvent.auctionClaimed(auction, oldAuction, oldVolunteerId));
        return auction;
    }

    @Override
    public @org.jspecify.annotations.NonNull AssignmentSwitchRequest executeTrade(@org.jspecify.annotations.NonNull AssignmentSwitchRequest trade) {
        // delete inverse trade if exists
        List<AssignmentSwitchRequest> inverse = assignmentSwitchRequestDao.findInverseTrade(trade)
            .stream()
            .filter(x -> x.getStatus().equals(TradeStatus.OPEN))
            .toList();
        if (inverse.size() > 1) {
            throw new IllegalStateException("more than one inverse trade is open" + inverse);
        }
        if (!inverse.isEmpty()) {
            assignmentSwitchRequestDao.delete(inverse.get(0));
        }
        // cancel all trades for involved assignments
        cancelOtherTrades(trade);

        // update fields and id of assignments
        AssignmentSwitchRequest oldTrade = TradeMapper.shallowCopy(trade);
        reassign(trade.getOfferingAssignment(), oldTrade.getRequestedAssignment().getAssignedVolunteer());
        reassign(trade.getRequestedAssignment(), oldTrade.getOfferingAssignment().getAssignedVolunteer());
        trade.setStatus(TradeStatus.ACCEPTED);
        assignmentSwitchRequestDao.save(trade);


        rewardPointsService.onAssignmentReassignedTrade(trade.getOfferingAssignment(), trade.getRequestedAssignment());

        publisher.publishEvent(AssignmentSwitchEvent.assignmentSwitched(oldTrade.getRequestedAssignment(), oldTrade.getOfferingAssignment()));

        return trade;
    }

    @Override
    public void cancelOtherTrades(@org.jspecify.annotations.NonNull AssignmentSwitchRequest trade) {
        // this trade does not need to be excluded because it will be set to ACCEPTED in the next step
        assignmentSwitchRequestDao.cancelTradesForAssignment(trade.getRequestedAssignment());
        assignmentSwitchRequestDao.cancelTradesForAssignment(trade.getOfferingAssignment());
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
    public @org.jspecify.annotations.NonNull Assignment accept(@org.jspecify.annotations.NonNull Assignment assignment) {
        if (assignment.getStatus() != AssignmentStatus.REQUEST_FOR_ASSIGNMENT) {
            throw new IllegalArgumentException("Assignment must be an assignment request");
        }

        // update reward points
        rewardPointsService.onAssignmentAccepted(assignment);

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment = assignmentDao.save(assignment);

        publisher.publishEvent(PositionSlotVolunteerEvent.positionSlotJoined(assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
        return assignment;
    }

    @Override
    public @org.jspecify.annotations.NonNull Assignment assign(@NonNull PositionSlot positionSlot, @NonNull Volunteer volunteer, @NonNull PositionSlotRequestDto requestDto) {
        // create assignment
        Assignment assignment = Assignment.of(positionSlot, volunteer, AssignmentStatus.ACCEPTED);

        // update reward points
        rewardPointsService.onAssignmentCreated(
            assignment,
            requestDto.getAcceptedRewardPointsConfigHash());

        // close trades where this slot was offered to current user
        assignmentSwitchRequestDao.cancelTradesForOfferedPositionAndRequestedUser(positionSlot.getId(), volunteer.getId());
        assignment = assignmentDao.save(assignment);


        publisher.publishEvent(PositionSlotVolunteerEvent.positionSlotJoined(assignment.getPositionSlot(), volunteer.getId()));
        return assignment;
    }

    @Override
    @Transactional
    public void unassignInternal(@org.jspecify.annotations.NonNull Assignment assignment) {
        //ACT: update reward points
        rewardPointsService.onAssignmentRemoved(
            assignment
        );

        //ACT: cancel trades & remove assignment
        assignmentSwitchRequestDao.cancelTradesForAssignment(assignment);
        assignmentDao.delete(assignment);

        //NOTIFY: publish event
        publisher.publishEvent(PositionSlotVolunteerEvent.positionSlotLeft(assignment.getPositionSlot(), assignment.getAssignedVolunteer().getId()));
    }

    @Override
    @Transactional
    public void unassignAllAuctions(@org.jspecify.annotations.NonNull ShiftPlan shiftPlan) {
        Collection<Assignment> auctions = assignmentDao.findAuctionsByShiftPlanId(shiftPlan.getId());

        auctions.forEach(auction -> {
            // update reward points
            rewardPointsService.onAssignmentRemoved(
                auction
            );

            // publish event
            publisher.publishEvent(PositionSlotVolunteerEvent.positionSlotLeft(auction.getPositionSlot(), auction.getAssignedVolunteer().getId()));
        });

        assignmentDao.deleteAll(auctions);
    }

    @Override
    @Transactional
    public void declineAllSignupRequests(@org.jspecify.annotations.NonNull ShiftPlan shiftPlan) {
        Collection<Assignment> requests = assignmentDao.findSignupRequestsByShiftPlanId(shiftPlan.getId());

        // publish events
        requests.forEach(request ->
            publisher.publishEvent(PositionSlotVolunteerEvent.positionSlotJoinRequestDenied(request.getPositionSlot(), request.getAssignedVolunteer().getId())));

        assignmentDao.deleteAll(requests);
    }

    @Override
    public @org.jspecify.annotations.NonNull Collection<Assignment> getAllAssignmentsForUser(@org.jspecify.annotations.NonNull ShiftPlan plan, @org.jspecify.annotations.NonNull Volunteer volunteer) {
        return assignmentDao.findAssignmentsForShiftPlanAndUser(plan.getId(), volunteer.getId());
    }
}
