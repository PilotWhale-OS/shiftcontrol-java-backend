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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.event.EventScheduleDto;
import at.shiftcontrol.shiftservice.dto.event.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.event.EventsDashboardOverviewDto;
import at.shiftcontrol.shiftservice.service.DashboardService;
import at.shiftcontrol.shiftservice.service.event.EventCloneService;
import at.shiftcontrol.shiftservice.service.event.EventExportService;
import at.shiftcontrol.shiftservice.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EventEndpoint {
    private final ApplicationUserProvider userProvider;
    private final EventService eventService;
    private final EventCloneService eventCloneService;
    private final EventExportService eventExportService;
    private final DashboardService dashboardService;

    @GetMapping("/{eventId}")
    @Operation(
        operationId = "getEventById",
        description = "Find event by id"
    )
    public EventDto getEvent(@PathVariable String eventId) {
        return eventService.getEvent(ConvertUtil.idToLong(eventId));
    }

    @GetMapping()
    @Operation(
        operationId = "getAllEvents",
        description = "Find all (volunteer related) events"
    )
    public Collection<EventDto> getAllEvents() {
        return eventService.search(null);
    }
    //Todo: Add search capability in future

    @GetMapping("/{eventId}/schedule")
    @Operation(
        operationId = "getEventSchedule",
        description = "Get the schedule of an event"
    )
    public EventScheduleDto getEventSchedule(@PathVariable String eventId, @Valid EventScheduleDaySearchDto searchDto) {
        return eventService.getEventSchedule(ConvertUtil.idToLong(eventId), searchDto);
    }

    @PostMapping()
    @Operation(
        operationId = "createEvent",
        description = "Create a new event"
    )
    public EventDto createEvent(@RequestBody @Valid EventModificationDto modificationDto) {
        return eventService.createEvent(modificationDto);
    }

    @PutMapping("/{eventId}")
    @Operation(
        operationId = "updateEvent",
        description = "Update an existing event"
    )
    public EventDto updateEvent(@PathVariable String eventId, @RequestBody @Valid EventModificationDto modificationDto) {
        return eventService.updateEvent(ConvertUtil.idToLong(eventId), modificationDto);
    }

    @DeleteMapping("/{eventId}")
    @Operation(
        operationId = "deleteEvent",
        description = "Delete an existing event"
    )
    public void deleteEvent(@PathVariable String eventId) {
        eventService.deleteEvent(ConvertUtil.idToLong(eventId));
    }

    @PostMapping("/{eventId}/clone")
    @Operation(
        operationId = "cloneEvent",
        description = "Clone an existing event"
    )
    public EventDto cloneEvent(@PathVariable String eventId) {
        return eventCloneService.cloneEvent(ConvertUtil.idToLong(eventId));
    }

    @GetMapping("/{eventId}/shift-plans-overview")
    @Operation(
        operationId = "getShiftPlansOverviewOfEvent",
        description = "Get an overview of all (volunteer related) shift plans of an event including statistics and reward points for the current user"
    )
    public EventShiftPlansOverviewDto getAllShiftPlanOverviewsOfEvent(@PathVariable String eventId) {
        return eventService.getEventShiftPlansOverview(ConvertUtil.idToLong(eventId), userProvider.getCurrentUser().getUserId());
    }

    @GetMapping("/dashboard")
    @Operation(
        operationId = "getEventsDashboard",
        description = "Get (volunteer related) dashboard data for all events"
    )
    public EventsDashboardOverviewDto getEventsDashboard() {
        return dashboardService.getDashboardOverviewsOfAllShiftPlans(userProvider.getCurrentUser().getUserId());
    }

    @GetMapping("{eventId}/export")
    @Operation(
        operationId = "exportEventData",
        description = "Export event data for external use"
    )
    public ResponseEntity<Resource> exportEventData(@PathVariable String eventId, @RequestParam String format) {
        var export = eventExportService.exportEvent(ConvertUtil.idToLong(eventId), format);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + export.getFileName())
            // filename will be set in frontend regardless of this value because header value is not used, but it is good practice to set it here anyway
            .contentType(export.getMediaType())
            .body(new InputStreamResource(export.getExportStream()));
    }

    // TODO delete this test controller!!!
    @GetMapping("/trust-alert")
    @Operation(
        operationId = "sendTestEvent",
        description = "sends a test event to the event bus"
    )
    public boolean sendTestEvent(@RequestParam String event) {
        return eventService.sendTestEvent(event);
    }
}
