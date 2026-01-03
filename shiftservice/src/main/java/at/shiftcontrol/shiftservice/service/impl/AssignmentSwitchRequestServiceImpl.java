package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;

import at.shiftcontrol.shiftservice.event.events.AssignmentSwitchEvent;
import at.shiftcontrol.shiftservice.service.ApplicationEventService;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dao.AssignmentSwitchRequestDao;
import at.shiftcontrol.shiftservice.dao.PositionSlotDao;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import at.shiftcontrol.shiftservice.mapper.TradeMapper;
import at.shiftcontrol.shiftservice.service.AssignmentSwitchRequestService;
import at.shiftcontrol.shiftservice.service.EligibilityService;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Service
@RequiredArgsConstructor
public class AssignmentSwitchRequestServiceImpl implements AssignmentSwitchRequestService {
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;
    private final PositionSlotDao positionSlotDao;
    private final EligibilityService eligibilityService;
    private final ApplicationEventService eventService;

    @Override
    public TradeDto getTradeById(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found")); // TODO move check into dao?
        return TradeMapper.toDto(trade);
    }

    @Override
    public Collection<TradeDto> createShiftTrade(TradeCreateDto tradeCreateDto) throws NotFoundException, ConflictException {
        Volunteer currentUser = Volunteer.builder().id(getDummyUserID()).build(); // TODO get current User

        // get requested PositionSlot
        PositionSlot requestedPositionSlot = positionSlotDao.findById(ConvertUtil.idToLong(tradeCreateDto.getRequestedPositionSlotId()))
            .orElseThrow(() -> new NotFoundException("requested PositionSlot not found"));
        // get offering assignment
        PositionSlot offeredPositionSlot = positionSlotDao.findById(ConvertUtil.idToLong(tradeCreateDto.getOfferedPositionSlotId()))
            .orElseThrow(() -> new NotFoundException("offered PositionSlot not found"));
        Assignment offeredAssignment = offeredPositionSlot.getAssignments().stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId().equals(currentUser.getId())).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("not assigned to offered Position"));

        // check if eligible and for conflicts
        validateTradePossible(offeredPositionSlot, requestedPositionSlot, currentUser);

        // TODO implement (check for status)

        // create switch requests for each requested user
        Collection<AssignmentSwitchRequest> trades = requestedPositionSlot.getAssignments().stream()
            .filter(assignment ->
                tradeCreateDto.getRequestedVolunteers().stream().anyMatch(
                    volunteer -> Objects.equals(volunteer.getId(), String.valueOf(assignment.getAssignedVolunteer().getId()))))
            .map(requestedAssignment -> createAssignmentSwitchRequest(requestedAssignment, offeredAssignment)).toList();

        return TradeMapper.toDto(assignmentSwitchRequestDao.saveAll(trades));
    }

    @Override
    public TradeDto acceptShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException, ConflictException {
        Volunteer currentUser = Volunteer.builder().id(getDummyUserID()).build(); // TODO get current User

        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        trade.setStatus(TradeStatus.ACCEPTED);

        // check if eligible and for conflicts
        validateTradePossible(trade.getOfferingAssignment().getPositionSlot(), trade.getRequestedAssignment().getPositionSlot(), currentUser);

        // TODO implement (check for status)

        // update assignments
        executeTrade(trade);

        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public TradeDto declineShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        trade.setStatus(TradeStatus.REJECTED);
        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public TradeDto cancelShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
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

    private void validateTradePossible(PositionSlot offeredPositionSlot, PositionSlot requestedPositionSlot, Volunteer currentUser) throws ConflictException {
        if (offeredPositionSlot.getShift().getShiftPlan().getId() != requestedPositionSlot.getShift().getShiftPlan().getId()) {
            throw new IllegalArgumentException("position slots belong to different shift plans");
        }
        // check if user is eligible for the requested position slot
        eligibilityService.validateSignUpStateForTrade(requestedPositionSlot, currentUser);
        // check if any current assignments overlap with requested position slot
        eligibilityService.validateHasConflictingAssignmentsExcludingSlot(
            currentUser.getId(), requestedPositionSlot.getShift().getStartTime(), requestedPositionSlot.getShift().getEndTime(), offeredPositionSlot.getId());
    }

    private void executeTrade(AssignmentSwitchRequest trade) {
        Volunteer requestedAssignmentVolunteer = trade.getRequestedAssignment().getAssignedVolunteer();
        Volunteer offeringAssignmentVolunteer = trade.getOfferingAssignment().getAssignedVolunteer();
        updateVolunteer(trade.getOfferingAssignment(), offeringAssignmentVolunteer);
        updateVolunteer(trade.getRequestedAssignment(), requestedAssignmentVolunteer);
        trade.setStatus(TradeStatus.ACCEPTED);

        eventService.publishEvent(AssignmentSwitchEvent.of(trade.getRequestedAssignment(), trade.getOfferingAssignment()), "assignment.switch");
    }

    private void updateVolunteer(Assignment assignment, Volunteer volunteer) {
        assignment.setAssignedVolunteer(volunteer);
        assignment.getId().setVolunteerId(volunteer.getId());
    }

    public String getDummyUserID() {
        return "28c02050-4f90-4f3a-b1df-3c7d27a166e5";
    }
}
