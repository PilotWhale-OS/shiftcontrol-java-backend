package at.shiftcontrol.shiftservice.endpoint;

import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.EventOverviewDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events")
@RequiredArgsConstructor
public class EventEndpoint {
    @GetMapping()
// TODO:    @Secured({"planner.event.read", "volunteer.event.read"})
    @Operation(
        operationId = "findAllEvents",
        description = "Find all (volunteer related) events"
    )
    public Collection<EventOverviewDto> findAllEvents() {
        return null; // TODO: implement
    }
    
    @GetMapping("/{eventId}/shift-plans")
    // TODO Security
    @Operation(
        operationId = "findAllShiftPlansOfEvent",
        description = "Find all (volunteer related) shift plans of an event"
    )
    public EventShiftPlansOverviewDto findAllShiftPlansOfEvent(@PathVariable String eventId) {
        return null; // TODO: implement
    }

    @GetMapping("/{eventId}/shift-plans/{shiftPlanId}/dashboard")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDashboardData",
        description = "Get dashboard data for a specific shift plan of an event"
    )
    public DashboardOverviewDto getShiftPlanDashboardData(@PathVariable String eventId, @PathVariable String shiftPlanId) {
        return null; // TODO: implement
    }
}
