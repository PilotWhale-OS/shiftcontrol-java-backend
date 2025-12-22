package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotJoinErrorDto;
import at.shiftcontrol.shiftservice.dto.UserPreferenceUpdateDto;
import at.shiftcontrol.shiftservice.dto.UserShiftPreferenceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/position-slots/{positionSlotId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PositionSlotEndpoint {
    @GetMapping
    // TODO Security
    @Operation(
        operationId = "getPositionSlot",
        description = "Get details for a specific position slot in a shift"
    )
    public PositionSlotDto getPositionSlot(@PathVariable String positionSlotId) {
        return null; // TODO: implement
    }

    @PostMapping("/join")
    // TODO Security
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
    public AssignmentDto joinPositionSlot(@PathVariable String positionSlotId) {
        return null; // TODO: implement
    }

    @PutMapping("/preference")
    // TODO Security
    @Operation(
        operationId = "setPositionSlotPreference",
        description = "Set preference for a specific position slot"
    )
    public UserShiftPreferenceDto setPositionSlotPreference(@PathVariable String positionSlotId, @RequestBody UserPreferenceUpdateDto preferenceUpdateDto) {
        return null; // TODO: implement
    }

    @GetMapping("/assignments")
    // TODO Security
    @Operation(
        operationId = "getPositionSlotAssignments",
        description = "Get assignments for a specific position slot"
    )
    public Collection<AssignmentDto> getPositionSlotAssignments(@PathVariable String positionSlotId) {
        return null; // TODO: implement
    }
}
