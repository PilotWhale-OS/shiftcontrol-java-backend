package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.TradeCandidatesDto;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.mapper.TradeMapper;
import at.shiftcontrol.shiftservice.service.AssignmentSwitchRequestService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/trades", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PositionSlotTradeEndpoint {
    private final AssignmentSwitchRequestService assignmentSwitchRequestService;
    private final ApplicationUserProvider userProvider;

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getTradeById",
        description = "Get trade by id"
    )
    public TradeDto getTradeById(TradeDto tradeDto) throws NotFoundException {
        return assignmentSwitchRequestService.getTradeById(TradeMapper.toEntityId(tradeDto));
    }

    @GetMapping("/slots-to-offer/{positionSlotId}")
    // TODO Security
    @Operation(
        operationId = "getPositionSlotsToOffer",
        description = "Get position slots that can be offered in a trade for the given position slot, based on eligible volunteers"
    )
    public Collection<TradeCandidatesDto> getPositionSlotsToOffer(@PathVariable String positionSlotId) throws NotFoundException {
        return assignmentSwitchRequestService.getPositionSlotsToOffer(
            ConvertUtil.idToLong(positionSlotId),
            userProvider.getCurrentUser().getUserId());
    }

    @PostMapping()
    // TODO Security
    @Operation(
        operationId = "createTrade",
        description = "Create trade request for a specific position slot in a shift"
    )
    public Collection<TradeDto> createTrade(@RequestBody TradeCreateDto tradeCreateDto) throws NotFoundException, ConflictException {
        return assignmentSwitchRequestService.createTrade(
            tradeCreateDto,
            userProvider.getCurrentUser().getUserId());
    }

    @PutMapping("/accept")
    // TODO Security
    @Operation(
        operationId = "acceptTrade",
        description = "Accept a trade request for a specific position slot in a shift"
    )
    public TradeDto acceptTrade(@RequestBody TradeDto tradeDto) throws NotFoundException, ConflictException {
        return assignmentSwitchRequestService.acceptTrade(
            TradeMapper.toEntityId(tradeDto),
            userProvider.getCurrentUser().getUserId());
    }

    @PutMapping("/decline")
    // TODO Security
    @Operation(
        operationId = "declineTrade",
        description = "Decline a trade request for a specific position slot in a shift"
    )
    public TradeDto declineTrade(@RequestBody TradeDto tradeDto) throws NotFoundException {
        return assignmentSwitchRequestService.declineTrade(
            TradeMapper.toEntityId(tradeDto),
            userProvider.getCurrentUser().getUserId());
    }

    @PutMapping("/cancel")
    // TODO Security
    @Operation(
        operationId = "cancelTrade",
        description = "Cancel a request for a specific position slot in a shift"
    )
    public void cancelTrade(TradeDto tradeDto) throws NotFoundException {
        assignmentSwitchRequestService.cancelTrade(
            TradeMapper.toEntityId(tradeDto),
            userProvider.getCurrentUser().getUserId());
    }
}
