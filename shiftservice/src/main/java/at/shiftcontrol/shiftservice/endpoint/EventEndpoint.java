package at.shiftcontrol.shiftservice.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.EventOverviewDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.service.EventService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events")
@RequiredArgsConstructor
public class EventEndpoint {
    private final EventService eventService;

    @GetMapping()
    // TODO:    @Secured({"planner.event.read", "volunteer.event.read"})
    @Operation(
        operationId = "findAllEvents",
        description = "Find all (volunteer related) events"
    )
    public List<EventOverviewDto> findAllEvents() {
        return eventService.search(null);
    }

    @GetMapping("/{eventId}/shift-plans")
    // TODO Security
    @Operation(
        operationId = "findAllShiftPlansOfEvent",
        description = "Find all (volunteer related) shift plans of an event"
    )
    public EventShiftPlansOverviewDto findAllShiftPlansOfEvent(@PathVariable String eventId) throws NotFoundException {
        //Todo: pass user id from security context
        return eventService.getEventShiftPlansOverview(ConvertUtil.idToLong(eventId), -1);
    }
}
