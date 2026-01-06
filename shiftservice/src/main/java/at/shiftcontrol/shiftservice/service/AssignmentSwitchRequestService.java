package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

public interface AssignmentSwitchRequestService {
    TradeDto getTradeById(AssignmentSwitchRequestId id);

    Collection<TradeCandidatesDto> getPositionSlotsToOffer(long requestedPositionSlotId, String currentUserId);

    Collection<TradeDto> createTrade(TradeCreateDto tradeCreateDto, String currentUserId);

    TradeDto acceptTrade(AssignmentSwitchRequestId id, String currentUserId);

    TradeDto declineTrade(AssignmentSwitchRequestId id, String currentUserId);

    TradeDto cancelTrade(AssignmentSwitchRequestId id, String currentUserId);
}
