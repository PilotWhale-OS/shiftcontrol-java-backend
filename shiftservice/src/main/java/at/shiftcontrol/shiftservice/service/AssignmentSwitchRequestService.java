package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.trade.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeDto;

public interface AssignmentSwitchRequestService {
    TradeDto getTradeById(long id);

    Collection<TradeCandidatesDto> getPositionSlotsToOffer(long requestedPositionSlotId, String currentUserId);

    Collection<TradeDto> createTrade(TradeCreateDto tradeCreateDto, String currentUserId);

    TradeDto acceptTrade(long id, String currentUserId);

    TradeDto declineTrade(long id, String currentUserId);

    TradeDto cancelTrade(long id, String currentUserId);
}
