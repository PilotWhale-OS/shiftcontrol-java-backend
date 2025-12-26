package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dto.EventDto;
import at.shiftcontrol.shiftservice.dto.EventShiftPlansOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintCreateDto;
import at.shiftcontrol.shiftservice.dto.TimeConstraintDto;
import at.shiftcontrol.shiftservice.service.EventService;
import at.shiftcontrol.shiftservice.service.TimeConstraintService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EventEndpoint {
    private final ApplicationUserProvider userProvider;
    private final EventService eventService;
    private final TimeConstraintService timeConstraintService;

    @GetMapping()
    //TODO: @Secured({"planner.event.read", "volunteer.event.read"})
    @Operation(
        operationId = "getAllEvents",
        description = "Find all (volunteer related) events"
    )
    public List<EventDto> getAllEvents() throws NotFoundException {
        return eventService.search(null, userProvider.getCurrentUser().getUserId());
    }
    //Todo: Add search capability in future

    @GetMapping("/{eventId}/shift-plans")
    //TODO Security
    @Operation(
        operationId = "getShiftPlansOfEvent",
        description = "Find all (volunteer related) shift plans of an event"
    )
    public List<ShiftPlanDto> getShiftPlansOfEvent(@PathVariable String eventId) throws NotFoundException {
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

    @GetMapping("/{eventId}/time-constraints")
    // TODO Security
    @Operation(
        operationId = "getTimeConstraints",
        description = "Get time constraints of the current user"
    )
    public Collection<TimeConstraintDto> getTimeConstraints(@PathVariable String eventId) {
        return timeConstraintService.getTimeConstraints(userProvider.getCurrentUser().getUserId(), ConvertUtil.idToLong(eventId));
    }

    @PostMapping("/{eventId}/time-constraints")
    // TODO Security
    @Operation(
        operationId = "createTimeConstraint",
        description = "Create a new time constraint for the current user"
    )
    public TimeConstraintDto createUnavailability(@PathVariable String eventId, @RequestBody TimeConstraintCreateDto createDto) throws ConflictException {
        return timeConstraintService.createTimeConstraint(
            createDto,
            userProvider.getCurrentUser().getUserId(),
            ConvertUtil.idToLong(eventId)
        );
    }

    @DeleteMapping("/{eventId}/time-constraints/{timeConstraintId}")
    // TODO Security
    @Operation(
        operationId = "deleteUnavailability",
        description = "Delete an existing unavailability period of the current user"
    )
    public void deleteUnavailability(@PathVariable String eventId, @PathVariable String timeConstraintId) throws NotFoundException {
        timeConstraintService.delete(ConvertUtil.idToLong(timeConstraintId));
    }
}
