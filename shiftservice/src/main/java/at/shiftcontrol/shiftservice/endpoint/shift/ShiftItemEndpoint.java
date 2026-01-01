package at.shiftcontrol.shiftservice.endpoint.shift;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotModificationDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import at.shiftcontrol.shiftservice.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shifts/{shiftId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftItemEndpoint {
    private final ApplicationUserProvider userProvider;
    private final ShiftService shiftService;
    private final PositionSlotService positionSlotService;

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getShiftDetails",
        description = "Get details for a specific shift of a shift plan"
    )
    public ShiftDetailsDto getShiftDetails(@PathVariable String shiftId) throws NotFoundException {
        return shiftService.getShiftDetails(ConvertUtil.idToLong(shiftId), userProvider.getCurrentUser().getUserId());
    }
    
    @PutMapping()
    // TODO Security
    @Operation(
        operationId = "updateShift",
        description = "Update a specific shift of a shift plan"
    )
    public ShiftDto updateShift(@PathVariable String shiftId, @RequestBody @Valid ShiftModificationDto shiftModificationDto)
        throws NotFoundException, ForbiddenException {
        return shiftService.updateShift(ConvertUtil.idToLong(shiftId), shiftModificationDto);
    }

    @DeleteMapping()
    // TODO Security
    @Operation(
        operationId = "deleteShift",
        description = "Delete a specific shift of a shift plan"
    )
    public void deleteShift(@PathVariable String shiftId) throws NotFoundException, ForbiddenException {
        shiftService.deleteShift(ConvertUtil.idToLong(shiftId));
    }


    @PostMapping("/position-slot")
    // TODO Security
    @Operation(
        operationId = "createPositionSlotInShift",
        description = "Create position slot in a specific shift"
    )
    public PositionSlotDto createPositionSlotInShift(@PathVariable String shiftId, @RequestBody @Valid PositionSlotModificationDto positionSlotModificationDto)
        throws NotFoundException, ForbiddenException {
        return positionSlotService.createPositionSlot(ConvertUtil.idToLong(shiftId), positionSlotModificationDto);
    }

    //     @PostMapping("/auction")
    //     // TODO Security
    //     @Operation(
    //         operationId = "auctionShift",
    //         description = "Auction a specific position slot in a shift"
    //     )
    //     public AuctionDto auctionShift(@PathVariable String shiftId) {
    //         return null; // TODO: implement
    //     }

    //     @PostMapping("/join")
    //     // TODO Security
    //     @Operation(
    //         operationId = "joinShift",
    //         description = "Join a specific shift"
    //     )
    //     public ShiftDto joinShift(@PathVariable String shiftId) {
    //         return null; // TODO: implement
    //     }

    //     @PutMapping("/preference")
    //     // TODO Security
    //     @Operation(
    //         operationId = "setShiftPreference",
    //         description = "Set preference for a specific shift"
    //     )
    //     public UserShiftPreferenceDto setShiftPreference(@PathVariable String shiftId, @RequestBody UserPreferenceUpdateDto preferenceUpdateDto) {
    //         return null; // TODO: implement
    //     }
}
