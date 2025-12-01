package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}")
@RequiredArgsConstructor
public class ShiftPlanEndpoint {
    @GetMapping("/dashboard")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDashboard",
        description = "Get (volunteer related) dashboard data for a specific shift plan of an event"
    )
    public DashboardOverviewDto getShiftPlanDashboard(@PathVariable String shiftPlanId) {
        return null; // TODO: implement
    }

    @GetMapping("/schedule")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanSchedule",
        description = "Get (volunteer related) schedule data for a specific shift plan of an event"
    )
    public ShiftPlanScheduleDto getShiftPlanSchedule(@PathVariable String shiftPlanId, ShiftPlanScheduleSearchDto shiftPlanScheduleSearchDto) {
        return null; // TODO: implement
    }

    @GetMapping("/details")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDetails",
        description = "Get detailed information about a specific shift plan of an event"
    )
    public ShiftPlanDto getShiftPlanDetails(@PathVariable String shiftPlanId) {
        return null; // TODO: implement
    }

    @PostMapping("/join")
    // TODO Security
    @Operation(
        operationId = "joinShiftPlan",
        description = "Join a shift plan using an invite code"
    )
    public ShiftPlanDto joinShiftPlan(@PathVariable String shiftPlanId, @RequestBody ShiftPlanJoinRequestDto requestDto) {
        return null; // TODO: implement
    }
}
