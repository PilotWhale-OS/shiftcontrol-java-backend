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
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.service.EventService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events")
@RequiredArgsConstructor
public class EventEndpoint {
    private final ApplicationUserProvider userProvider;
    private final EventService eventService;

    @GetMapping()
    //TODO: @Secured({"planner.event.read", "volunteer.event.read"})
    @Operation(
        operationId = "getAllEvents",
        description = "Find all (volunteer related) events"
    )
    public List<EventDto> getAllEvents() {
        return eventService.search(null);
    }

    //Todo: Add search capability in future

    @GetMapping("/{eventId}/shift-plans")
    //TODO Security
    @Operation(
        operationId = "getShiftPlansOfEvent",
        description = "Find all (volunteer related) shift plans of an event"
    )
    public List<ShiftPlanDto> getShiftPlansOfEvent(@PathVariable String eventId) {
        return null; //TODO: implement
    }


    @GetMapping("/{eventId}/shift-plans-overview")
    //TODO Security
    @Operation(
        operationId = "getShiftPlansOverviewOfEvent",
        description = "Get an overview of all shift plans of an event including statistics and reward points for the current user"
    )
    public EventShiftPlansOverviewDto getAllShiftPlanOverviewsOfEvent(@PathVariable String eventId) throws NotFoundException {
        return eventService.getEventShiftPlansOverview(ConvertUtil.idToLong(eventId), userProvider.getCurrentUser().getUserId());
    }
}
