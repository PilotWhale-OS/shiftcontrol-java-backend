package at.shiftcontrol.shiftservice.endpoint.activity;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.activity.ActivityDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivityModificationDto;
import at.shiftcontrol.shiftservice.dto.activity.ActivitySuggestionDto;
import at.shiftcontrol.shiftservice.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
@RequestMapping(value = "api/v1/events/{eventId}/activities", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ActivityCollectionEndpoint {
    private final ActivityService activityService;

    @GetMapping()
    // TODO Security
    @Operation(
        operationId = "getActivitiesForEvent",
        description = "Get all activities for a specific event"
    )
    public Collection<ActivityDto> getActivitiesForEvent(@PathVariable String eventId) throws NotFoundException {
        return activityService.getActivitiesForEvent(ConvertUtil.idToLong(eventId));
    }

    @PostMapping()
    // TODO Security
    @Operation(
        operationId = "createActivity",
        description = "Create a new activity for a specific event"
    )
    public ActivityDto createActivity(@PathVariable String eventId, @RequestBody @Valid ActivityModificationDto modificationDto)
        throws NotFoundException {
        return activityService.createActivity(ConvertUtil.idToLong(eventId), modificationDto);
    }

    @PostMapping("/suggest")
    // TODO Security
    @Operation(
        operationId = "suggestActivitiesForShift",
        description = "Suggest activities for a specific shift based on its time and optional name filter"
    )
    public Collection<ActivityDto> suggestActivitiesForShift(@PathVariable String eventId, @RequestBody @Valid ActivitySuggestionDto suggestionDto)
        throws NotFoundException {
        return activityService.suggestActivitiesForShift(ConvertUtil.idToLong(eventId), suggestionDto);
    }

}
