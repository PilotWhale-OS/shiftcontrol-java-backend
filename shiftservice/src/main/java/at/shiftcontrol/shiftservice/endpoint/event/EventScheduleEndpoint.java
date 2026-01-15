package at.shiftcontrol.shiftservice.endpoint.event;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.EventScheduleLayoutDto;
import at.shiftcontrol.shiftservice.service.event.EventScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events/{eventId}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EventScheduleEndpoint {
    private final EventScheduleService eventScheduleService;

    @GetMapping("/layout")
    @Operation(
        operationId = "getEventScheduleLayout",
        description = "Get (volunteer related) schedule layout data for a specific event"
    )
    public EventScheduleLayoutDto getShiftPlanScheduleLayout(@PathVariable String eventId, @Valid EventScheduleFilterDto filterDto) {
        return eventScheduleService.getEventScheduleLayout(ConvertUtil.idToLong(eventId), filterDto);
    }

    @GetMapping("/content")
    @Operation(
        operationId = "getEventScheduleContent",
        description = "Get (volunteer related) schedule content data for a specific day of a specific event"
    )
    public EventScheduleContentDto getShiftPlanScheduleContent(@PathVariable String eventId,
                                                               @Valid EventScheduleDaySearchDto eventScheduleDaySearchDto) {
        return eventScheduleService.getEventScheduleContent(ConvertUtil.idToLong(eventId), eventScheduleDaySearchDto);
    }

    @GetMapping("/filters")
    @Operation(
        operationId = "getEventScheduleFilterValues",
        description = "Get available filter values for the schedule of a specific event"
    )
    public EventScheduleFilterValuesDto getShiftPlanScheduleFilterValues(@PathVariable String eventId) {
        return eventScheduleService.getEventScheduleFilterValues(ConvertUtil.idToLong(eventId));
    }
}
