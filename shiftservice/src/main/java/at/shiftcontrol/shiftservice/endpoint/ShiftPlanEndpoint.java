package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    @GetMapping("{shiftPlanId}/dashboard")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDashboard",
        description = "Get (volunteer related) dashboard data for a specific shift plan of an event"
    )
    public DashboardOverviewDto getShiftPlanDashboard(@PathVariable String shiftPlanId) throws NotFoundException, ForbiddenException {
        return shiftPlanService.getDashboardOverview(ConvertUtil.idToLong(shiftPlanId));
    }

    @GetMapping("{shiftPlanId}/schedule")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanSchedule",
        description = "Get (volunteer related) schedule data for a specific shift plan of an event"
    )
    public ShiftPlanScheduleDto getShiftPlanSchedule(@PathVariable String shiftPlanId,
                                                     @RequestBody(required = false) ShiftPlanScheduleSearchDto shiftPlanScheduleSearchDto)
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
    
    @PostMapping("{shiftPlanId}/invite")
    // TODO Security
    @Operation(
        operationId = "createShiftPlanInvite",
        description = "Create an invite code for a specific shift plan of an event"
    )
    public ShiftPlanInviteCreateResponseDto createShiftPlanInvite(@PathVariable String shiftPlanId, @RequestBody ShiftPlanInviteCreateRequestDto requestDto) {
        return shiftPlanService.createShiftPlanInviteCode(ConvertUtil.idToLong(shiftPlanId), requestDto);
    }

    // TODO endpoints to list all codes for shiftPlan + revoke code (safe revoked or delete?)
    // TODO add roles for invite codes so that user gets assigned specific roles when joining via invite code (to avoid many manual role assignments)
    
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
