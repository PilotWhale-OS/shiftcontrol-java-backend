package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.service.DashboardService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftPlanEndpoint {
    private final ShiftPlanService shiftPlanService;
    private final DashboardService dashboardService;

    @GetMapping("/{shiftPlanId}/dashboard")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDashboard",
        description = "Get (volunteer related) dashboard data for a specific shift plan of an event"
    )
    public ShiftPlanDashboardOverviewDto getShiftPlanDashboard(@PathVariable String shiftPlanId) throws NotFoundException, ForbiddenException {
        return dashboardService.getDashboardOverviewOfShiftPlan(ConvertUtil.idToLong(shiftPlanId));
    }

    @GetMapping("/{shiftPlanId}/schedule")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanSchedule",
        description = "Get (volunteer related) schedule data for a specific shift plan of an event"
    )
    public ShiftPlanScheduleDto getShiftPlanSchedule(@PathVariable String shiftPlanId, @Valid ShiftPlanScheduleSearchDto shiftPlanScheduleSearchDto)
        throws NotFoundException, ForbiddenException {
        return shiftPlanService.getShiftPlanSchedule(ConvertUtil.idToLong(shiftPlanId), shiftPlanScheduleSearchDto);
    }

    @GetMapping("/schedule/filters")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanScheduleFilterValues",
        description = "Get available filter values for the schedule of a specific shift plan of an event"
    )
    public ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(@PathVariable String shiftPlanId) throws NotFoundException {
        return shiftPlanService.getShiftPlanScheduleFilterValues(ConvertUtil.idToLong(shiftPlanId));
    }

    @PostMapping("/{shiftPlanId}/invite")
    // TODO Security
    @Operation(
        operationId = "createShiftPlanInvite",
        description = "Create an invite code for a specific shift plan of an event"
    )
    public ShiftPlanInviteCreateResponseDto createShiftPlanInvite(@PathVariable String shiftPlanId,
                                                                  @RequestBody @Valid ShiftPlanInviteCreateRequestDto requestDto)
        throws ForbiddenException, NotFoundException {
        return shiftPlanService.createShiftPlanInviteCode(ConvertUtil.idToLong(shiftPlanId), requestDto);
    }

    @DeleteMapping("/invites/{inviteCode}")
    // TODO Security
    @Operation(
        operationId = "revokeShiftPlanInvite",
        description = "Revoke an invite code for a specific shift plan of an event (soft delete)"
    )
    public void revokeShiftPlanInvite(@PathVariable String inviteCode) throws ForbiddenException, NotFoundException {
        shiftPlanService.revokeShiftPlanInviteCode(inviteCode);
    }

    @GetMapping("/invites/{inviteCode}")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanInviteDetails",
        description = "Get details about a specific invite code for a shift plan"
    )
    public ShiftPlanInviteDto getShiftPlanInviteDetails(@PathVariable String inviteCode) throws NotFoundException, ForbiddenException {
        return shiftPlanService.getShiftPlanInviteDetails(inviteCode);
    }

    // endpoint to list all codes for shiftPlan
    @GetMapping("/{shiftPlanId}/invites")
    // TODO Security
    @Operation(
        operationId = "listShiftPlanInvites",
        description = "List all invite codes for a specific shift plan of an event"
    )
    public Collection<ShiftPlanInviteDto> listShiftPlanInvites(@PathVariable String shiftPlanId)
        throws ForbiddenException, NotFoundException {
        return shiftPlanService.listShiftPlanInvites(ConvertUtil.idToLong(shiftPlanId));
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
