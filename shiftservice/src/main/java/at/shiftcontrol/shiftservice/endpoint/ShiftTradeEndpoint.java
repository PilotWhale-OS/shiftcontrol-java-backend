package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/trades")
@RequiredArgsConstructor
public class ShiftTradeEndpoint {
    @GetMapping("/{tradeId}")
    // TODO Security
    @Operation(
        operationId = "getTradeById",
        description = "Get trade by id"
    )
    public TradeDto getTradeById(@PathVariable String tradeId) {
        return null; // TODO Implement
    }

    @PostMapping()
    // TODO Security
    @Operation(
        operationId = "createShiftTrade",
        description = "Create trade request for a specific position slot in a shift"
    )
    public TradeDto createShiftTrade(@RequestBody TradeCreateDto tradeCreateDto) {
        return null; // TODO Implement
    }

    @PostMapping("/{tradeId}/accept")
    // TODO Security
    @Operation(
        operationId = "acceptShiftTrade",
        description = "Accept a trade request for a specific position slot in a shift"
    )
    public TradeDto acceptShiftTrade(@PathVariable String tradeId) {
        return null; // TODO Implement
    }

    @PostMapping("/{tradeId}/decline")
    // TODO Security
    @Operation(
        operationId = "declineShiftTrade",
        description = "Decline a trade request for a specific position slot in a shift"
    )
    public TradeDto declineShiftTrade(@PathVariable String tradeId) {
        return null; // TODO Implement
    }

    @DeleteMapping("/{tradeId}")
    // TODO Security
    @Operation(
        operationId = "cancelShiftTrade",
        description = "Cancel a request for a specific position slot in a shift"
    )
    public void cancelShiftTrade(@PathVariable String tradeId) {
        // TODO Implement
    }
}
