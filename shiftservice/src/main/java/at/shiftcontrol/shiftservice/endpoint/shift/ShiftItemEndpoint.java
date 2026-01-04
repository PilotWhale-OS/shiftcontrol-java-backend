package at.shiftcontrol.shiftservice.endpoint.shift;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDetailsDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;
import at.shiftcontrol.shiftservice.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "shift-endpoint"
)
@Slf4j
@RestController
@RequestMapping(value = "api/v1/shifts/{shiftId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftItemEndpoint {
    private final ApplicationUserProvider userProvider;
    private final ShiftService shiftService;

    @GetMapping()
    @Operation(
        operationId = "getShiftDetails",
        description = "Get details for a specific shift of a shift plan"
    )
    public ShiftDetailsDto getShiftDetails(@PathVariable String shiftId) throws NotFoundException {
        return shiftService.getShiftDetails(ConvertUtil.idToLong(shiftId), userProvider.getCurrentUser().getUserId());
    }

    @PutMapping()
    @Operation(
        operationId = "updateShift",
        description = "Update a specific shift of a shift plan"
    )
    public ShiftDto updateShift(@PathVariable String shiftId, @RequestBody @Valid ShiftModificationDto shiftModificationDto)
        throws NotFoundException, ForbiddenException {
        return shiftService.updateShift(ConvertUtil.idToLong(shiftId), shiftModificationDto);
    }

    @DeleteMapping()
    @Operation(
        operationId = "deleteShift",
        description = "Delete a specific shift of a shift plan"
    )
    public void deleteShift(@PathVariable String shiftId) throws NotFoundException, ForbiddenException {
        shiftService.deleteShift(ConvertUtil.idToLong(shiftId));
    }

    //     @PostMapping("/auction")
    //     @Operation(
    //         operationId = "auctionShift",
    //         description = "Auction a specific position slot in a shift"
    //     )
    //     public AuctionDto auctionShift(@PathVariable String shiftId) {
    //         return null; // TODO: implement
    //     }

    //     @PostMapping("/join")
    //     @Operation(
    //         operationId = "joinShift",
    //         description = "Join a specific shift"
    //     )
    //     public ShiftDto joinShift(@PathVariable String shiftId) {
    //         return null; // TODO: implement
    //     }

    //     @PutMapping("/preference")
    //     @Operation(
    //         operationId = "setShiftPreference",
    //         description = "Set preference for a specific shift"
    //     )
    //     public UserShiftPreferenceDto setShiftPreference(@PathVariable String shiftId, @RequestBody UserPreferenceUpdateDto preferenceUpdateDto) {
    //         return null; // TODO: implement
    //     }
}
