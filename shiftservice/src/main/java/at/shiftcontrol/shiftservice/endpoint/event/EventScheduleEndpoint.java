package at.shiftcontrol.shiftservice.endpoint.event;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleLayoutDto;
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
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EventScheduleEndpoint {
    private final EventScheduleService eventScheduleService;

    @GetMapping("/layout")
    @Operation(
        operationId = "getShiftPlanScheduleLayout",
        description = "Get (volunteer related) schedule layout data for a specific shift plan of an event"
    )
    public ShiftPlanScheduleLayoutDto getShiftPlanScheduleLayout(@PathVariable String shiftPlanId, @Valid ShiftPlanScheduleFilterDto filterDto) {
        return eventScheduleService.getShiftPlanScheduleLayout(ConvertUtil.idToLong(shiftPlanId), filterDto);
    }

    @GetMapping("/content")
    @Operation(
        operationId = "getShiftPlanScheduleContent",
        description = "Get (volunteer related) schedule content data for a specific day of a specific shift plan of an event"
    )
    public ShiftPlanScheduleContentDto getShiftPlanScheduleContent(@PathVariable String shiftPlanId,
                                                                   @Valid ShiftPlanScheduleDaySearchDto shiftPlanScheduleSearchDto) {
        return eventScheduleService.getShiftPlanScheduleContent(ConvertUtil.idToLong(shiftPlanId), shiftPlanScheduleSearchDto);
    }

    @GetMapping("/filters")
    @Operation(
        operationId = "getShiftPlanScheduleFilterValues",
        description = "Get available filter values for the schedule of a specific shift plan of an event"
    )
    public ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(@PathVariable String shiftPlanId) {
        return eventScheduleService.getShiftPlanScheduleFilterValues(ConvertUtil.idToLong(shiftPlanId));
    }
}
