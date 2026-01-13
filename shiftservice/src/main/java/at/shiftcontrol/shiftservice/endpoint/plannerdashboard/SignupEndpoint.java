package at.shiftcontrol.shiftservice.endpoint.plannerdashboard;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.AssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentFilterDto;
import at.shiftcontrol.shiftservice.dto.plannerdashboard.AssignmentRequestDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.service.positionslot.PlannerPositionSlotService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SignupEndpoint {
    private final PlannerPositionSlotService positionSlotService;

    @GetMapping("/requests")
    @Operation(
        operationId = "getSlots",
        description = "Get assignments for an Event filtered by the AssignmentStatus"
    )
    public Collection<AssignmentRequestDto> getSlots(@PathVariable String shiftPlanId, @Valid AssignmentFilterDto filterDto) {
        return positionSlotService.getSlots(ConvertUtil.idToLong(shiftPlanId), filterDto);
    }

    @PostMapping("/requests/{positionSlotId}/accept")
    @Operation(
        operationId = "acceptRequest",
        description = "Accept  the slot request"
    )
    public void acceptRequest(@PathVariable String shiftPlanId, @PathVariable String positionSlotId, String userid) {
        positionSlotService.acceptRequest(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(positionSlotId), userid);
    }

    @PostMapping("/requests/{positionSlotId}/decline")
    @Operation(
        operationId = "declineRequest",
        description = "Decline the slot request"
    )
    public void declineRequest(@PathVariable String shiftPlanId, @PathVariable String positionSlotId, String userid) {
        positionSlotService.declineRequest(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(positionSlotId), userid);
    }

    @GetMapping("/assignable-users/{positionSlotId}")
    @Operation(
        operationId = "getAssignableUsers",
        description = "Gets all users that could be assigned to the given slot"
    )
    public Collection<VolunteerDto> getAssignableUsers(@PathVariable String shiftPlanId, @PathVariable String positionSlotId) {
        return positionSlotService.getAssignableUsers(positionSlotId);
    }

    @PostMapping("/assign")
    @Operation(
        operationId = "assignUsersToSlot",
        description = "Assigns the given users to the given slot"
    )
    public Collection<AssignmentDto> assignUsersToSlot(@PathVariable String shiftPlanId, @RequestBody AssignmentAssignDto assignmentAssignDto) {
        return positionSlotService.assignUsersToSlot(assignmentAssignDto);
    }
}
