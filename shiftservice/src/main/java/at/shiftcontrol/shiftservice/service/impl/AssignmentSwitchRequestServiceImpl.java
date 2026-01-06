package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.event.RoutingKeys;
import at.shiftcontrol.shiftservice.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.shiftservice.event.events.TradeEvent;
import at.shiftcontrol.shiftservice.mapper.PositionSlotAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.TradeMapper;
import at.shiftcontrol.shiftservice.service.AssignmentSwitchRequestService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.service.rewardpoints.RewardPointsService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.LockStatus;
import at.shiftcontrol.shiftservice.type.TradeStatus;
import at.shiftcontrol.shiftservice.util.SecurityHelper;

@Service
@RequiredArgsConstructor
public class AssignmentSwitchRequestServiceImpl implements AssignmentSwitchRequestService {
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;
    private final AssignmentDao assignmentDao;
    private final PositionSlotDao positionSlotDao;
    private final VolunteerDao volunteerDao;
    private final EligibilityService eligibilityService;
    private final RewardPointsService rewardPointsService;
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher publisher;

    @Override
    public TradeDto getTradeById(AssignmentSwitchRequestId id) {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.getById(id);
        return TradeMapper.toDto(trade);
    }

    @Override
    public Collection<TradeCandidatesDto> getPositionSlotsToOffer(long requestedPositionSlotId, String currentUserId) {
        // get requested PositionSlot
        PositionSlot requestedPositionSlot = positionSlotDao.getById(requestedPositionSlotId);

        securityHelper.assertUserIsVolunteer(requestedPositionSlot);

        // get volunteers assigned to PositionSlot
        Collection<Volunteer> assignedVolunteers = requestedPositionSlot.getAssignments().stream().map(Assignment::getAssignedVolunteer).toList();
        // return if no volunteers exist to trade with
        if (assignedVolunteers.isEmpty()) {
            return List.of();
        }
        // check if user is already assigned to requested position slot
        if (assignedVolunteers.stream().anyMatch(v -> currentUserId.equals(v.getId()))) {
            throw new IllegalArgumentException("already assigned to position slot");
        }

        // only check for locked, eligibility and conflict of current user when creating actual trade

        // get all assignments in this shift-plan the current user can offer
        Collection<Assignment> assignments = assignmentDao.findAssignmentsForShiftPlanAndUser(
            requestedPositionSlot.getShift().getShiftPlan().getId(), currentUserId);

        // for each assignment, check to which volunteers (of the requested slot) it can be offered
        Collection<TradeCandidatesDto> slotsToOffer = new LinkedList<>();
        for (Assignment a : assignments) {
            // only offer the position to eligible volunteers with no conflicts
            Collection<Volunteer> usersToTradeWith = getPossibleUsersForTrade(a.getPositionSlot(), requestedPositionSlot, assignedVolunteers);
            if (!usersToTradeWith.isEmpty()) {
                slotsToOffer.add(
                    positionSlotAssemblingMapper.tradeCandidatesDto(a.getPositionSlot(), usersToTradeWith));
            }
        }

        // remove candidates where the current user already requested a trade
        return removeExistingTrades(slotsToOffer, requestedPositionSlotId, currentUserId);
    }

    private Collection<Volunteer> getPossibleUsersForTrade(PositionSlot offeredPositionSlot, PositionSlot requestedPositionSlot,
                                                           Collection<Volunteer> volunteers) {
        return volunteers.stream().filter(v -> isTradePossibleForUser(offeredPositionSlot, requestedPositionSlot, v)).toList();
    }

    private boolean isTradePossibleForUser(PositionSlot offeredPositionSlot, PositionSlot requestedPositionSlot, Volunteer volunteer) {
        // check if eligible
        boolean eligible = eligibilityService.isTradePossible(offeredPositionSlot, volunteer);
        // check for conflicts
        Collection<Assignment> conflicts = eligibilityService.getConflictingAssignmentsExcludingSlot(
            volunteer.getId(), offeredPositionSlot.getShift().getStartTime(), offeredPositionSlot.getShift().getEndTime(), requestedPositionSlot.getId());

        return eligible && conflicts.isEmpty();
    }

    private Collection<TradeCandidatesDto> removeExistingTrades(Collection<TradeCandidatesDto> slotsToOffer, long requestedPositionSlotId,
                                                                String currentUserId) {
        // check for already existing trades in status OPEN
        Collection<AssignmentSwitchRequest> existingTrades =
            assignmentSwitchRequestDao.findOpenTradesForRequestedPositionAndOfferingUser(requestedPositionSlotId, currentUserId);

        // convert to set of keys for easier lookup
        Set<AssignmentSwitchRequestId> existingTradeKeys =
            existingTrades.stream()
                .map(tr -> AssignmentSwitchRequestId.of(
                    tr.getOfferingAssignment(),
                    tr.getRequestedAssignment()
                ))
                .collect(Collectors.toSet());

        // remove all volunteers for each possible slot to offer, where a trade already exists
        //  this does not check for existing inverse trade requests
        return slotsToOffer.stream()
            .map(candidate -> {
                List<AccountInfoDto> filteredVolunteers =
                    candidate.getAssignedVolunteers().stream()
                        .filter(volunteer -> {
                            AssignmentSwitchRequestId key = AssignmentSwitchRequestId.of(
                                AssignmentId.of(
                                    ConvertUtil.idToLong(candidate.getPositionSlotId()),
                                    currentUserId),
                                AssignmentId.of(
                                    requestedPositionSlotId,
                                    volunteer.getId())
                            );
                            // removes volunteers from the candidate list, where a trade already exists
                            return !existingTradeKeys.contains(key);
                        })
                        .toList();
                return new TradeCandidatesDto(
                    candidate.getPositionSlotId(),
                    filteredVolunteers
                );
            })
            // remove dto if no volunteers are left
            .filter(c -> !c.getAssignedVolunteers().isEmpty())
            .toList();
    }

    @Override
    @Transactional
    public Collection<TradeDto> createTrade(TradeCreateDto tradeCreateDto, String currentUserId) {
        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.getById(currentUserId);

        // get requested PositionSlot
        PositionSlot requestedPositionSlot = positionSlotDao.getById(ConvertUtil.idToLong(tradeCreateDto.getRequestedPositionSlotId()));
        // get offering assignment
        PositionSlot offeredPositionSlot = positionSlotDao.getById(ConvertUtil.idToLong(tradeCreateDto.getOfferedPositionSlotId()));
        // check if shifts are locked
        if (isPositionSlotLocked(requestedPositionSlot) || isPositionSlotLocked(offeredPositionSlot)) {
            throw new IllegalStateException("trade not possible, shift is locked");
        }

        Assignment offeredAssignment = offeredPositionSlot.getAssignments().stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId().equals(currentUser.getId())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("not assigned to offered Position"));

        // check if eligible and for conflicts
        validateTradePossible(offeredPositionSlot, requestedPositionSlot, currentUser);

        // get Volunteer entity for users to create trades with
        Collection<Volunteer> requestedVolunteers = getVolunteersToTradeWith(requestedPositionSlot, tradeCreateDto.getRequestedVolunteers());
        for (Volunteer v : requestedVolunteers) {
            // check if eligible and for conflicts
            validateTradePossible(requestedPositionSlot, offeredPositionSlot, v);
        }

        // create switch requests for each requested user
        Collection<AssignmentSwitchRequest> trades = requestedPositionSlot.getAssignments().stream()
            .filter(assignment ->
                tradeCreateDto.getRequestedVolunteers().stream().anyMatch(
                    volunteer -> Objects.equals(volunteer.getId(), String.valueOf(assignment.getAssignedVolunteer().getId()))))
            .map(requestedAssignment -> createAssignmentSwitchRequest(offeredAssignment, requestedAssignment)).toList();

        // no need to check for existing trades, status will just be updated

        // check if trade in other direction already exists
        // --> if exists, executing trade would result in same primary key
        //      therefore do not create second trade and accept first one
        for (AssignmentSwitchRequest trade : trades) {
            Optional<AssignmentSwitchRequest> inverse = getInverseTrade(trade.getId());
            if (inverse.isPresent()) {
                return List.of(acceptTrade(inverse.get().getId(), currentUserId));
            }
        }

        trades = assignmentSwitchRequestDao.saveAll(trades);

        trades.forEach(trade -> publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CREATED,
            Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                   "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
        )));
        return TradeMapper.toDto(trades);
    }

    private Collection<Volunteer> getVolunteersToTradeWith(PositionSlot positionSlot, Collection<VolunteerDto> volunteerDtos) {
        Set<String> dtoUserIds = volunteerDtos.stream()
            .map(VolunteerDto::getId)
            .collect(Collectors.toSet());
        return positionSlot.getAssignments().stream()
            .map(Assignment::getAssignedVolunteer)
            .filter(v -> dtoUserIds.contains(v.getId()))
            .toList();
    }

    @Override
    @Transactional
    public TradeDto acceptTrade(AssignmentSwitchRequestId id, String currentUserId) {
        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.getById(currentUserId); // todo fix to correct security helper function

        // get trade
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.getById(id);

        // check if volunteer is owner of requested slot
        if (!trade.getRequestedAssignment().getAssignedVolunteer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("current user is not part of the trade");
        }

        // check if shift is locked
        if (isTradeLocked(trade)) {
            throw new IllegalStateException("trade not possible, shift is locked");
        }

        // check if trade is open
        if (!trade.getStatus().equals(TradeStatus.OPEN)) {
            throw new IllegalArgumentException("trade not open");
        }

        // check for access, eligibility and conflicts for both users
        validateTradePossible(trade.getRequestedAssignment().getPositionSlot(), trade.getOfferingAssignment().getPositionSlot(),
            trade.getRequestedAssignment().getAssignedVolunteer()); // current user
        validateTradePossible(trade.getOfferingAssignment().getPositionSlot(), trade.getRequestedAssignment().getPositionSlot(),
            trade.getOfferingAssignment().getAssignedVolunteer()); // other user

        // delete inverse trade if exists, because accepting the other would result in the same primary key !
        getInverseTrade(id).ifPresent(assignmentSwitchRequestDao::delete);

        // cancel all trades for involved assignments
        cancelOtherTrades(trade);

        // update assignments
        executeTrade(trade);

        rewardPointsService.onAssignmentReassignedTrade(trade.getOfferingAssignment(), trade.getRequestedAssignment());

        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public TradeDto declineTrade(AssignmentSwitchRequestId id, String currentUserId) {
        // get trade
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.getById(id);
        // check if user can decline
        if (!trade.getRequestedAssignment().getAssignedVolunteer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("not involved in trade");
        }
        if (!trade.getStatus().equals(TradeStatus.OPEN)) {
            throw new IllegalStateException("trade not open");
        }

        trade.setStatus(TradeStatus.REJECTED);

        trade = assignmentSwitchRequestDao.save(trade);
        publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_DECLINED,
            Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
        ));
        return TradeMapper.toDto(trade);
    }

    @Override
    public TradeDto cancelTrade(AssignmentSwitchRequestId id, String currentUserId) {
        // get trade
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.getById(id);
        // check if user can cancel
        if (!trade.getOfferingAssignment().getAssignedVolunteer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("not involved in trade");
        }
        if (!trade.getStatus().equals(TradeStatus.OPEN)) {
            throw new IllegalStateException("trade not open");
        }

        trade.setStatus(TradeStatus.CANCELED);

        trade = assignmentSwitchRequestDao.save(trade);
        publisher.publishEvent(TradeEvent.of(RoutingKeys.format(RoutingKeys.TRADE_REQUEST_CANCELED,
            Map.of("requestedVolunteerId", trade.getRequestedAssignment().getAssignedVolunteer().getId(),
                "offeringVolunteerId", trade.getOfferingAssignment().getAssignedVolunteer().getId())), trade
        ));
        return TradeMapper.toDto(trade);
    }

    private AssignmentSwitchRequest createAssignmentSwitchRequest(Assignment offering, Assignment requested) {
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(offering.getId(), requested.getId());
        return new AssignmentSwitchRequest(
            id,
            offering,
            requested,
            TradeStatus.OPEN,
            Instant.now()
        );
    }

    private void validateTradePossible(PositionSlot ownedSlot, PositionSlot slotToBeTaken, Volunteer volunteer) {
        // check if position slots belong to same shift plan
        if (ownedSlot.getShift().getShiftPlan().getId() != slotToBeTaken.getShift().getShiftPlan().getId()) {
            throw new IllegalArgumentException("position slots belong to different shift plans");
        }

        // check if volunteer has access to shift plan
        securityHelper.assertUserIsVolunteer(slotToBeTaken);

        // check if user is eligible for the requested position slot
        eligibilityService.validateIsTradePossible(slotToBeTaken, volunteer);

        // check if any current assignments overlap with requested position slot
        eligibilityService.validateHasConflictingAssignmentsExcludingSlot(
            volunteer.getId(), slotToBeTaken.getShift().getStartTime(), slotToBeTaken.getShift().getEndTime(), ownedSlot.getId());
    }

    private boolean isTradeLocked(AssignmentSwitchRequest trade) {
        return isPositionSlotLocked(trade.getRequestedAssignment().getPositionSlot())
            || isPositionSlotLocked(trade.getOfferingAssignment().getPositionSlot());
    }

    private boolean isPositionSlotLocked(PositionSlot slot) {
        return slot.getShift().getShiftPlan().getLockStatus().equals(LockStatus.LOCKED);
    }

    private void cancelOtherTrades(AssignmentSwitchRequest trade) {
        // this trade does not need to be excluded because it will be set to ACCEPTED in the next step
        assignmentSwitchRequestDao.cancelTradesForAssignment(
            trade.getRequestedAssignment().getPositionSlot().getId(), trade.getRequestedAssignment().getAssignedVolunteer().getId());
        assignmentSwitchRequestDao.cancelTradesForAssignment(
            trade.getOfferingAssignment().getPositionSlot().getId(), trade.getOfferingAssignment().getAssignedVolunteer().getId());
    }

    private void executeTrade(AssignmentSwitchRequest trade) {
        Volunteer requestedAssignmentVolunteer = trade.getRequestedAssignment().getAssignedVolunteer();
        Volunteer offeringAssignmentVolunteer = trade.getOfferingAssignment().getAssignedVolunteer();
        updateVolunteer(trade.getOfferingAssignment(), offeringAssignmentVolunteer);
        updateVolunteer(trade.getRequestedAssignment(), requestedAssignmentVolunteer);
        // in case an assignment was up for auction
        trade.getOfferingAssignment().setStatus(AssignmentStatus.ACCEPTED);
        trade.getRequestedAssignment().setStatus(AssignmentStatus.ACCEPTED);
        // !!! trade requests in status ACCEPTED have a different key compared to the other states (volunteers swapped)
        //      changing an ACCEPTED trade back to OPEN would mean a new trade request to swap the assignments back
        trade.setStatus(TradeStatus.ACCEPTED);

        publisher.publishEvent(AssignmentSwitchEvent.of(trade.getRequestedAssignment(), trade.getOfferingAssignment()));
    }

    private void updateVolunteer(Assignment assignment, Volunteer volunteer) {
        assignment.setAssignedVolunteer(volunteer);
        assignment.getId().setVolunteerId(volunteer.getId());
    }

    private Optional<AssignmentSwitchRequest> getInverseTrade(AssignmentSwitchRequestId id) {
        return assignmentSwitchRequestDao.findById(
            new AssignmentSwitchRequestId(
                id.getRequested(),
                id.getOffering()
            )
        );
    }
}
