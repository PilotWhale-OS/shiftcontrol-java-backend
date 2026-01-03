package at.shiftcontrol.shiftservice.endpoint.shiftplan;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "shift-plan-invite-endpoint"
)
@Slf4j
@RestController
@RequestMapping(value = "api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftPlanInviteItemEndpoint {
    private final ShiftPlanService shiftPlanService;

    @PostMapping("/invites/{inviteId}")
    // TODO Security
    @Operation(
        operationId = "revokeShiftPlanInvite",
        description = "Revoke an invite code for a specific shift plan of an event (soft delete)"
    )
    public void revokeShiftPlanInvite(@PathVariable String inviteId) throws ForbiddenException, NotFoundException {
        shiftPlanService.revokeShiftPlanInvite(ConvertUtil.idToLong(inviteId));
    }

    @DeleteMapping("/invites/{inviteId}")
    // TODO Security
    @Operation(
        operationId = "deleteShiftPlanInvite",
        description = "Delete an invite code for a specific shift plan of an event (hard delete)"
    )
    public void deleteShiftPlanInvite(@PathVariable String inviteId) throws ForbiddenException, NotFoundException {
        shiftPlanService.deleteShiftPlanInvite(ConvertUtil.idToLong(inviteId));
    }


    @GetMapping("/invites/{inviteCode}")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanInviteDetails",
        description = "Get details about a specific invite code for a shift plan"
    )
    public ShiftPlanJoinOverviewDto getShiftPlanInviteDetails(@PathVariable String inviteCode) throws NotFoundException, ForbiddenException {
        return shiftPlanService.getShiftPlanInviteDetails(inviteCode);
    }

    @PostMapping("/join")
    // TODO Security
    @Operation(
        operationId = "joinShiftPlan",
        description = "Join a shift plan using an invite code"
    )
    public ShiftPlanJoinOverviewDto joinShiftPlan(@RequestBody ShiftPlanJoinRequestDto requestDto) throws NotFoundException {
        return shiftPlanService.joinShiftPlanAsVolunteer(requestDto);
    }
}
