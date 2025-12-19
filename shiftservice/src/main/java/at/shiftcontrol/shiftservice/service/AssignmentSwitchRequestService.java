package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

public interface AssignmentSwitchRequestService {
    TradeDto getTradeById(AssignmentSwitchRequestId id) throws NotFoundException;

    Collection<TradeDto> createShiftTrade(TradeCreateDto tradeCreateDto) throws NotFoundException;

    TradeDto acceptShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException;

    TradeDto declineShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException;

    void cancelShiftTrade(AssignmentSwitchRequestId id) throws NotFoundException;
}
