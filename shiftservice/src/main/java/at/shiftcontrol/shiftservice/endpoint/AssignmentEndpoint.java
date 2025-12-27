package at.shiftcontrol.shiftservice.endpoint;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/position-slots/{positionSlotId}/assignments/{volunteerId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AssignmentEndpoint {
    @Deprecated
    @PostMapping("/auction")
    // TODO Security
    @Operation(
        operationId = "auctionPositionSlot",
        description = "Auction a specific position slot in a shift"
    )
    public AssignmentDto auctionAssignment(@PathVariable String positionSlotId, @PathVariable String volunteerId) {
        return null; // TODO: implement
    }
    // assignment functionality is implemented in PositionSlotEndpoint and PositionSlotTradeEndpoint
}
