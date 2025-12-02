package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.shiftservice.dto.AuctionDto;
import at.shiftcontrol.shiftservice.dto.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.ShiftDto;
import at.shiftcontrol.shiftservice.dto.UserPreferenceDto;
import at.shiftcontrol.shiftservice.dto.UserPreferenceUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shifts/{shiftId}")
@RequiredArgsConstructor
public class ShiftEndpoint {
    @GetMapping("/details")
    // TODO Security
    @Operation(
        operationId = "getShiftDetails",
        description = "Get details for a specific shift of a shift plan"
    )
    public ShiftDetailsDto getShiftDetails(@PathVariable String shiftId) {
        return null; // TODO: implement
    }

    @PostMapping("/auction")
    // TODO Security
    @Operation(
        operationId = "auctionShift",
        description = "Auction a specific position slot in a shift"
    )
    public AuctionDto auctionShift(@PathVariable String shiftId) {
        return null; // TODO: implement
    }

    @PostMapping("/join")
    // TODO Security
    @Operation(
        operationId = "joinShift",
        description = "Join a specific shift"
    )
    public ShiftDto joinShift(@PathVariable String shiftId) {
        return null; // TODO: implement
    }

    @PutMapping("/preference")
    // TODO Security
    @Operation(
        operationId = "setShiftPreference",
        description = "Set preference for a specific shift"
    )
    public UserPreferenceDto setShiftPreference(@PathVariable String shiftId, @RequestBody UserPreferenceUpdateDto preferenceUpdateDto) {
        return null; // TODO: implement
    }
}
