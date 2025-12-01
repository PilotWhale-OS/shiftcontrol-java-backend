package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
