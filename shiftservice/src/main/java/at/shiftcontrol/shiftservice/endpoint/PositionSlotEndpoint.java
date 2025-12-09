package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

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

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.UserPreferenceUpdateDto;
import at.shiftcontrol.shiftservice.dto.UserShiftPreferenceDto;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/position-slots/{positionSlotId}")
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
        description = "Join a specific position slot"
    )
    public PositionSlotDto joinPositionSlot(@PathVariable String positionSlotId) {
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
