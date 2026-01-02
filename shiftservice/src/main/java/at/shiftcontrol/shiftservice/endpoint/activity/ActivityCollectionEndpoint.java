package at.shiftcontrol.shiftservice.endpoint.activity;

import java.util.Collection;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.ActivityDto;
import at.shiftcontrol.shiftservice.dto.ActivitySuggestionDto;
import at.shiftcontrol.shiftservice.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ActivityCollectionEndpoint {
    private final ActivityService activityService;

    // TODO: Get all, create

    @PostMapping("/{eventId}/suggest-activities")
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
