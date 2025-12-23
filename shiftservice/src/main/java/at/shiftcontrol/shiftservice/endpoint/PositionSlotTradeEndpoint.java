package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
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

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getTradeById",
        description = "Get trade by id"
    )
    // TODO change body to params ?
    public TradeDto getTradeById(@RequestBody TradeDto tradeDto) throws NotFoundException {
        return assignmentSwitchRequestService.getTradeById(TradeMapper.toEntityId(tradeDto));
    }

    @PostMapping()
    // TODO Security
    @Operation(
        operationId = "createShiftTrade",
        description = "Create trade request for a specific position slot in a shift"
    )
    public Collection<TradeDto> createShiftTrade(@RequestBody TradeCreateDto tradeCreateDto) throws NotFoundException, ConflictException {
        return assignmentSwitchRequestService.createShiftTrade(tradeCreateDto);
    }

    @PostMapping("/accept")
    // TODO Security
    @Operation(
        operationId = "acceptShiftTrade",
        description = "Accept a trade request for a specific position slot in a shift"
    )
    public TradeDto acceptShiftTrade(@RequestBody TradeDto tradeDto) throws NotFoundException, ConflictException {
        return assignmentSwitchRequestService.acceptShiftTrade(TradeMapper.toEntityId(tradeDto));
    }

    @PostMapping("/decline")
    // TODO Security
    @Operation(
        operationId = "declineShiftTrade",
        description = "Decline a trade request for a specific position slot in a shift"
    )
    public TradeDto declineShiftTrade(@RequestBody TradeDto tradeDto) throws NotFoundException {
        return assignmentSwitchRequestService.declineShiftTrade(TradeMapper.toEntityId(tradeDto));
    }

    @DeleteMapping() // TODO trade does not get deleted ?
    // TODO Security
    @Operation(
        operationId = "cancelShiftTrade",
        description = "Cancel a request for a specific position slot in a shift"
    )
    public void cancelShiftTrade(@RequestBody TradeDto tradeDto) throws NotFoundException {
        assignmentSwitchRequestService.cancelShiftTrade(TradeMapper.toEntityId(tradeDto));
    }
}
