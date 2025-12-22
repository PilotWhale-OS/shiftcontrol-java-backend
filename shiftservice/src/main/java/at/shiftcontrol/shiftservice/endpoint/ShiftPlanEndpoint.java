package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
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
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftPlanEndpoint {
    private final ShiftPlanService shiftPlanService;

    @GetMapping("/dashboard")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDashboard",
        description = "Get (volunteer related) dashboard data for a specific shift plan of an event"
    )
    public DashboardOverviewDto getShiftPlanDashboard(@PathVariable String shiftPlanId) throws NotFoundException, ForbiddenException {
        return shiftPlanService.getDashboardOverview(ConvertUtil.idToLong(shiftPlanId));
    }

    @GetMapping("/schedule")
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

    @PostMapping("/join")
    // TODO Security
    @Operation(
        operationId = "joinShiftPlan",
        description = "Join a shift plan using an invite code"
    )
    public ShiftPlanJoinOverviewDto joinShiftPlan(@PathVariable String shiftPlanId, @RequestBody ShiftPlanJoinRequestDto requestDto) throws NotFoundException {
        return shiftPlanService.joinShiftPlan(ConvertUtil.idToLong(shiftPlanId), requestDto);
    }
}
