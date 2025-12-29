package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.AssignmentDao;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.PositionSlotAssemblingMapper;
import at.shiftcontrol.shiftservice.mapper.TradeMapper;
import at.shiftcontrol.shiftservice.service.AssignmentSwitchRequestService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import at.shiftcontrol.shiftservice.type.LockStatus;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Service
@RequiredArgsConstructor
public class AssignmentSwitchRequestServiceImpl implements AssignmentSwitchRequestService {
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;
    private final AssignmentDao assignmentDao;
    private final PositionSlotDao positionSlotDao;
    private final VolunteerDao volunteerDao;
    private final EligibilityService eligibilityService;
    private final PositionSlotAssemblingMapper positionSlotAssemblingMapper;

    @Override
    public TradeDto getTradeById(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found")); // TODO move check into dao?
        return TradeMapper.toDto(trade);
    }

    @Override
    public Collection<TradeCandidatesDto> getPositionSlotsToOffer(long requestedPositionSlotId, String currentUserId) throws NotFoundException {
        // get requested PositionSlot
        PositionSlot requestedPositionSlot = positionSlotDao.findById(requestedPositionSlotId)
            .orElseThrow(() -> new NotFoundException("requested PositionSlot not found"));
        // get volunteers assigned to PositionSlot
        Collection<Volunteer> assignedVolunteers = requestedPositionSlot.getAssignments().stream().map(Assignment::getAssignedVolunteer).toList();
        // return if no volunteers exist to trade with
        if (assignedVolunteers.isEmpty()) {
            return List.of();
        }
        // check if user is already assigned
        if (assignedVolunteers.stream().anyMatch(v -> currentUserId.equals(v.getId()))) {
            throw new IllegalArgumentException("already assigned to position slot");
        }

        // only check for locked, eligibility and conflict of current user when creating actual trade

        // get all assignments for current user in this shift-plan
        Collection<Assignment> assignments = assignmentDao.findAssignmentsForShiftPlanAndUser(
            requestedPositionSlot.getShift().getShiftPlan().getId(), currentUserId);

        // for each assignment, check to which volunteers (of the requested slot) it can be offered
        Collection<TradeCandidatesDto> slotsToOffer = new LinkedList<>();
        for (Assignment a : assignments) {
            Collection<Volunteer> usersToTradeWith = getPossibleUsersForTrade(a.getPositionSlot(), requestedPositionSlot, assignedVolunteers);
            if (!usersToTradeWith.isEmpty()) {
                slotsToOffer.add(
                    positionSlotAssemblingMapper.tradeCandidatesDto(a.getPositionSlot(), usersToTradeWith));
            }
        }

        return slotsToOffer;
    }

    private Collection<Volunteer> getPossibleUsersForTrade(PositionSlot offeredPositionSlot, PositionSlot requestedPositionSlot, Collection<Volunteer> volunteers) {
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

    @Override
    @Transactional
    public Collection<TradeDto> createTrade(TradeCreateDto tradeCreateDto, String currentUserId) throws NotFoundException, ConflictException {
        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("user not found"));

        // get requested PositionSlot
        PositionSlot requestedPositionSlot = positionSlotDao.findById(ConvertUtil.idToLong(tradeCreateDto.getRequestedPositionSlotId()))
            .orElseThrow(() -> new NotFoundException("requested PositionSlot not found"));
        // get offering assignment
        PositionSlot offeredPositionSlot = positionSlotDao.findById(ConvertUtil.idToLong(tradeCreateDto.getOfferedPositionSlotId()))
            .orElseThrow(() -> new NotFoundException("offered PositionSlot not found"));
        Assignment offeredAssignment = offeredPositionSlot.getAssignments().stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId().equals(currentUser.getId())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("not assigned to offered Position"));

        // check if shifts are locked
        if (isPositionSlotLocked(requestedPositionSlot) || isPositionSlotLocked(offeredPositionSlot)) {
            throw new IllegalStateException("trade not possible, shift is locked");
        }

        // check if eligible and for conflicts
        validateTradePossible(offeredPositionSlot, requestedPositionSlot, currentUser);

        Collection<Volunteer> requestedVolunteers = filterVolunteers(requestedPositionSlot, tradeCreateDto.getRequestedVolunteers());

        for (Volunteer v : requestedVolunteers) {
            validateTradePossible(requestedPositionSlot, offeredPositionSlot, v);
        }

        // create switch requests for each requested user
        Collection<AssignmentSwitchRequest> trades = requestedPositionSlot.getAssignments().stream()
            .filter(assignment ->
                tradeCreateDto.getRequestedVolunteers().stream().anyMatch(
                    volunteer -> Objects.equals(volunteer.getId(), String.valueOf(assignment.getAssignedVolunteer().getId()))))
            .map(requestedAssignment -> createAssignmentSwitchRequest(requestedAssignment, offeredAssignment)).toList();

        return TradeMapper.toDto(assignmentSwitchRequestDao.saveAll(trades));
    }

    private Collection<Volunteer> filterVolunteers(PositionSlot positionSlot, Collection<VolunteerDto> volunteerDtos) {
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
    public TradeDto acceptTrade(AssignmentSwitchRequestId id, String currentUserId) throws NotFoundException, ConflictException {
        // get current user (volunteer)
        Volunteer currentUser = volunteerDao.findByUserId(currentUserId)
            .orElseThrow(() -> new NotFoundException("user not found"));

        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));

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
        validateTradePossible(trade.getOfferingAssignment().getPositionSlot(), trade.getRequestedAssignment().getPositionSlot(),
            trade.getRequestedAssignment().getAssignedVolunteer());
        validateTradePossible(trade.getRequestedAssignment().getPositionSlot(), trade.getOfferingAssignment().getPositionSlot(),
            trade.getOfferingAssignment().getAssignedVolunteer());

        // cancel all trades for involved assignments
        cancelOtherTrades(trade);

        // update assignments
        executeTrade(trade);

        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public TradeDto declineTrade(AssignmentSwitchRequestId id, String currentUserId) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        if (!trade.getRequestedAssignment().getAssignedVolunteer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("not involved in trade");
        }
        if (!trade.getStatus().equals(TradeStatus.OPEN)) {
            throw new IllegalStateException("trade not open");
        }
        trade.setStatus(TradeStatus.REJECTED);
        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public TradeDto cancelTrade(AssignmentSwitchRequestId id, String currentUserId) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        if (!trade.getOfferingAssignment().getAssignedVolunteer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("not involved in trade");
        }
        if (!trade.getStatus().equals(TradeStatus.OPEN)) {
            throw new IllegalStateException("trade not open");
        }
        trade.setStatus(TradeStatus.CANCELED);
        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
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

    private void validateTradePossible(PositionSlot ownedSlot, PositionSlot slotToBeTaken, Volunteer volunteer) throws ConflictException {
        if (ownedSlot.getShift().getShiftPlan().getId() != slotToBeTaken.getShift().getShiftPlan().getId()) {
            throw new IllegalArgumentException("position slots belong to different shift plans");
        }

        // check if volunteer has access to shift plan
        eligibilityService.validateHasAccessToPositionSlot(slotToBeTaken, volunteer.getId());

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
        return slot.getShift().getLockStatus().equals(LockStatus.LOCKED);
    }

    private void cancelOtherTrades(AssignmentSwitchRequest trade) {
        assignmentSwitchRequestDao.cancelTradesForAssignment(
            trade.getRequestedAssignment().getPositionSlot().getId(), trade.getRequestedAssignment().getAssignedVolunteer().getId());
        assignmentSwitchRequestDao.cancelTradesForAssignment(
            trade.getOfferingAssignment().getPositionSlot().getId(), trade.getOfferingAssignment().getAssignedVolunteer().getId());
        // TODO exclude this trade?
    }

    private void executeTrade(AssignmentSwitchRequest trade) {
        Volunteer requestedAssignmentVolunteer = trade.getRequestedAssignment().getAssignedVolunteer();
        Volunteer offeringAssignmentVolunteer = trade.getOfferingAssignment().getAssignedVolunteer();
        updateVolunteer(trade.getOfferingAssignment(), offeringAssignmentVolunteer);
        updateVolunteer(trade.getRequestedAssignment(), requestedAssignmentVolunteer);
        // in case an assignment was up for auction
        trade.getOfferingAssignment().setStatus(AssignmentStatus.ACCEPTED);
        trade.getRequestedAssignment().setStatus(AssignmentStatus.ACCEPTED);
        trade.setStatus(TradeStatus.ACCEPTED);
    }

    private void updateVolunteer(Assignment assignment, Volunteer volunteer) {
        assignment.setAssignedVolunteer(volunteer);
        assignment.getId().setVolunteerId(volunteer.getId());
    }
}
