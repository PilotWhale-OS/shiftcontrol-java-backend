package at.shiftcontrol.shiftservice.endpoint.event;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.event.EventsDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.service.DashboardService;
import at.shiftcontrol.shiftservice.service.EventService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EventEndpoint {
    private final ApplicationUserProvider userProvider;
    private final EventService eventService;
    private final DashboardService dashboardService;

    @GetMapping()
    //TODO: @Secured({"planner.event.read", "volunteer.event.read"})
    @Operation(
        operationId = "getAllEvents",
        description = "Find all (volunteer related) events"
    )
    public Collection<EventDto> getAllEvents() throws NotFoundException {
        return eventService.search(null, userProvider.getCurrentUser().getUserId());
    }
    //Todo: Add search capability in future

    @PostMapping()
    // TODO: Security
    @Operation(
        operationId = "createEvent",
        description = "Create a new event"
    )
    public EventDto createEvent(@RequestBody @Valid EventModificationDto modificationDto) {
        return eventService.createEvent(modificationDto);
    }

    @PutMapping("/{eventId}")
    // TODO: Security
    @Operation(
        operationId = "updateEvent",
        description = "Update an existing event"
    )
    public EventDto updateEvent(@PathVariable String eventId, @RequestBody @Valid EventModificationDto modificationDto) throws NotFoundException {
        return eventService.updateEvent(ConvertUtil.idToLong(eventId), modificationDto);
    }

    @DeleteMapping("/{eventId}")
    // TODO: Security
    @Operation(
        operationId = "deleteEvent",
        description = "Delete an existing event"
    )
    public void deleteEvent(@PathVariable String eventId) throws NotFoundException {
        eventService.deleteEvent(ConvertUtil.idToLong(eventId));
    }


    @GetMapping("/{eventId}/shift-plans")
    //TODO Security
    @Operation(
        operationId = "getShiftPlansOfEvent",
        description = "Find all (volunteer related) shift plans of an event"
    )
    public Collection<ShiftPlanDto> getShiftPlansOfEvent(@PathVariable String eventId) throws NotFoundException {
        return eventService.getUserRelatedShiftPlansOfEvent(ConvertUtil.idToLong(eventId), userProvider.getCurrentUser().getUserId());
    }

    @GetMapping("/{eventId}/shift-plans-overview")
    //TODO Security
    @Operation(
        operationId = "getShiftPlansOverviewOfEvent",
        description = "Get an overview of all (volunteer related) shift plans of an event including statistics and reward points for the current user"
    )
    public EventShiftPlansOverviewDto getAllShiftPlanOverviewsOfEvent(@PathVariable String eventId) throws NotFoundException {
        return eventService.getEventShiftPlansOverview(ConvertUtil.idToLong(eventId), userProvider.getCurrentUser().getUserId());
    }

    @GetMapping("/dashboard")
    // TODO Security
    @Operation(
        operationId = "getEventsDashboard",
        description = "Get (volunteer related) dashboard data for all events"
    )
    public EventsDashboardOverviewDto getEventsDashboard() throws NotFoundException, ForbiddenException {
        return dashboardService.getDashboardOverviewsOfAllShiftPlans(userProvider.getCurrentUser().getUserId());
    }
}
