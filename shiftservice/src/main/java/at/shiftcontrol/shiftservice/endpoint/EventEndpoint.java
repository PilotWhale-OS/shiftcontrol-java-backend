package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.shiftservice.dto.EventOverviewDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;

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
}
