package at.shiftcontrol.shiftservice.endpoint.positionslot;

import java.util.Collection;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotModificationDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotPreferenceDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotPreferenceUpdateDto;
import at.shiftcontrol.shiftservice.dto.positionslot.PositionSlotRequestDto;
import at.shiftcontrol.shiftservice.service.PositionSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
    name = "position-slot-endpoint"
)
@Slf4j
@RestController
@RequestMapping(value = "api/v1/position-slots/{positionSlotId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PositionSlotItemEndpoint {
    private final PositionSlotService positionSlotService;
    private final ApplicationUserProvider userProvider;

    @GetMapping
    @Operation(
        operationId = "getPositionSlot",
        description = "Get details for a specific position slot in a shift"
    )
    public PositionSlotDto getPositionSlot(@PathVariable String positionSlotId) {
        return positionSlotService.findById(ConvertUtil.idToLong(positionSlotId));
    }

    @PutMapping()
    @Operation(
        operationId = "updatePositionSlot",
        description = "Update a specific position slot in a shift"
    )
    public PositionSlotDto updatePositionSlot(@PathVariable String positionSlotId, @RequestBody @Valid PositionSlotModificationDto modificationDto) {
        return positionSlotService.updatePositionSlot(ConvertUtil.idToLong(positionSlotId), modificationDto);
    }

    @DeleteMapping()
    @Operation(
        operationId = "deletePositionSlot",
        description = "Delete a specific position slot in a shift"
    )
    public void deletePositionSlot(@PathVariable String positionSlotId) {
        positionSlotService.deletePositionSlot(ConvertUtil.idToLong(positionSlotId));
    }

    @PostMapping("/join")
    @Operation(
        operationId = "joinPositionSlot",
        description = "Join a specific position slot",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successfully joined the position slot",
                content = @Content(
                    schema = @Schema(implementation = AssignmentDto.class)
                )
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - e.g., position slot is already full",
                content = @Content(
                    schema = @Schema(implementation = PositionSlotJoinErrorDto.class)
                )
            )
        }
    )
    public AssignmentDto joinPositionSlot(@PathVariable String positionSlotId, @RequestBody @Valid PositionSlotRequestDto requestDto) {
        return positionSlotService.join(
            ConvertUtil.idToLong(positionSlotId),
            userProvider.getCurrentUser().getUserId(),
            requestDto
        );
    }

    @PutMapping("/preference")
    @Operation(
        operationId = "setPositionSlotPreference",
        description = "Set preference for a specific position slot"
    )
    public PositionSlotPreferenceDto setPositionSlotPreference(
        @PathVariable String positionSlotId,
        @RequestBody @Valid PositionSlotPreferenceUpdateDto preferenceUpdateDto
    ) {
        positionSlotService.setPreference(
            userProvider.getCurrentUser().getUserId(),
            ConvertUtil.idToLong(positionSlotId),
            preferenceUpdateDto.getPreferenceValue()
        );
        return PositionSlotPreferenceDto.builder().preferenceValue(preferenceUpdateDto.getPreferenceValue()).build();
    }

    @GetMapping("/assignments")
    @Operation(
        operationId = "getPositionSlotAssignments",
        description = "Get assignments for a specific position slot"
    )
    public Collection<AssignmentDto> getPositionSlotAssignments(@PathVariable String positionSlotId) {
        return null; // TODO: implement
    }

    @PostMapping("/auction")
    @Operation(
        operationId = "auctionAssignment",
        description = "Put the logged in users assignment for the PositionSlot up for auction"
    )
    public AssignmentDto auctionAssignment(@PathVariable String positionSlotId) {
        return positionSlotService.createAuction(
            ConvertUtil.idToLong(positionSlotId),
            userProvider.getCurrentUser().getUserId());
    }

    @PostMapping("/claim-auction/{offeringUserId}")
    @Operation(
        operationId = "claimAuction",
        description = "Assign the logged in user to the auctions PositionSlot"
    )
    public AssignmentDto claimAssignment(
        @PathVariable String positionSlotId,
        @PathVariable String offeringUserId,
        @RequestBody @Valid PositionSlotRequestDto requestDto
    ) {
        return positionSlotService.claimAuction(
            ConvertUtil.idToLong(positionSlotId), offeringUserId,
            userProvider.getCurrentUser().getUserId(),
            requestDto);
    }

    @PostMapping("/cancel-auction")
    @Operation(
        operationId = "cancelAuction",
        description = "Cancel the logged in users auction for the PositionSlot"
    )
    public AssignmentDto cancelAuction(@PathVariable String positionSlotId) {
        return positionSlotService.cancelAuction(
            ConvertUtil.idToLong(positionSlotId),
            userProvider.getCurrentUser().getUserId());
    }
    // TODO positionSlot unassign
    // TODO positionSlot request unassign
    // TODO positionSlot request signup
}
