package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

public interface AssignmentSwitchRequestService {
    TradeDto getTradeById(AssignmentSwitchRequestId id) throws NotFoundException;

    Collection<TradeCandidatesDto> getPositionSlotsToOffer(long requestedPositionSlotId, String currentUserId) throws NotFoundException, ForbiddenException;

    Collection<TradeDto> createTrade(TradeCreateDto tradeCreateDto, String currentUserId) throws NotFoundException, ConflictException, ForbiddenException;

    TradeDto acceptTrade(AssignmentSwitchRequestId id, String currentUserId) throws NotFoundException, ConflictException, ForbiddenException;

    TradeDto declineTrade(AssignmentSwitchRequestId id, String currentUserId) throws NotFoundException;

    TradeDto cancelTrade(AssignmentSwitchRequestId id, String currentUserId) throws NotFoundException;
}
