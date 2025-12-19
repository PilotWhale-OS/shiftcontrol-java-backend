package at.shiftcontrol.shiftservice.service.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

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
import at.shiftcontrol.shiftservice.mapper.TradeMapper;
import at.shiftcontrol.shiftservice.service.AssignmentSwitchRequestService;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Service
@RequiredArgsConstructor
public class AssignmentSwitchRequestServiceImpl implements AssignmentSwitchRequestService {
    private final AssignmentSwitchRequestDao assignmentSwitchRequestDao;
    private final PositionSlotDao positionSlotDao;

    @Override
    public TradeDto getTradeById(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found")); // TODO move check into dao?
        return TradeMapper.toDto(trade);
    }

    @Override
    public Collection<TradeDto> createShiftTrade(TradeCreateDto tradeCreateDto) throws NotFoundException {
        // get requested PositionSlot
        PositionSlot requestedPositionSlot = positionSlotDao.findById(ConvertUtil.idToLong(tradeCreateDto.getRequestedPositionSlotId()))
            .orElseThrow(() -> new NotFoundException("requested PositionSlot not found"));
        // get offering assignment
        PositionSlot offeredPositionSlot = positionSlotDao.findById(ConvertUtil.idToLong(tradeCreateDto.getOfferedPositionSlotId()))
            .orElseThrow(() -> new NotFoundException("offered PositionSlot not found"));
        Assignment offeredAssignment = offeredPositionSlot.getAssignments().stream()
            .filter(assignment -> assignment.getAssignedVolunteer().getId() == 1L).findFirst() // TODO get current user
            .orElseThrow(() -> new IllegalArgumentException("not assigned to offered Position"));

        // create switch requests for each requested user
        Collection<AssignmentSwitchRequest> trades = requestedPositionSlot.getAssignments().stream()
            .filter(assignment ->
                tradeCreateDto.getRequestedVolunteers().stream().anyMatch(
                    volunteer -> Objects.equals(volunteer.getId(), String.valueOf(assignment.getAssignedVolunteer().getId()))))
            .map(requestedAssignment -> createAssignmentSwitchRequest(requestedAssignment, offeredAssignment)).toList();

        // TODO implement (check for conflicts, locked and status)
        return TradeMapper.toDtos(assignmentSwitchRequestDao.saveAll(trades));
    }

    @Override
    public TradeDto acceptShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        trade.setStatus(TradeStatus.ACCEPTED);
        // TODO implement (check for conflicts, locked and status)
        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public TradeDto declineShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        trade.setStatus(TradeStatus.REJECTED);
        // TODO implement (check for conflicts, locked and status)
        return TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
    }

    @Override
    public void cancelShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException {
        AssignmentSwitchRequest trade = assignmentSwitchRequestDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Trade not found"));
        trade.setStatus(TradeStatus.CANCELED);
        // TODO implement (check for conflicts, locked and status)
        TradeMapper.toDto(assignmentSwitchRequestDao.save(trade));
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
}
