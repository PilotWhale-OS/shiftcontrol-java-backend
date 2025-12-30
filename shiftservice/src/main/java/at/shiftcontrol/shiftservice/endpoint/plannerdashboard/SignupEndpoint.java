package at.shiftcontrol.shiftservice.endpoint.plannerdashboard;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.PositionSlotDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.service.positionslot.PlannerPositionSlotService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SignupEndpoint {
    private final PlannerPositionSlotService positionSlotService;

    @GetMapping("/requests")
    // TODO Security
    @Operation(
        operationId = "getSlots",
        description = "Get assignments for an Event filtered by the AssignmentStatus"
    )
    public Collection<PositionSlotDto> getSlots(@PathVariable String shiftPlanId, @Valid AssignmentFilterDto filterDto) throws NotFoundException {
        return positionSlotService.getSlots(ConvertUtil.idToLong(shiftPlanId), filterDto);
    }

    @PostMapping("/requests/{positionSlotId}/accept")
    // TODO Security
    @Operation(
        operationId = "acceptRequest",
        description = "Accept  the slot request"
    )
    public void acceptRequest(@PathVariable String shiftPlanId, @PathVariable String positionSlotId) {
        positionSlotService.acceptRequest(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(positionSlotId));
    }

    @PostMapping("/requests/{positionSlotId}/decline")
    // TODO Security
    @Operation(
        operationId = "declineRequest",
        description = "Decline the slot request"
    )
    public void declineRequest(@PathVariable String shiftPlanId, @PathVariable String positionSlotId) {
        positionSlotService.declineRequest(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(positionSlotId));
    }
}
