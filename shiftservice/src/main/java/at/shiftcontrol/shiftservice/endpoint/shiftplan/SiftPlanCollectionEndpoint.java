package at.shiftcontrol.shiftservice.endpoint.shiftplan;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanModificationDto;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;

@Tag(
    name = "shift-plan-endpoint"
)
@Slf4j
@RestController
@RequestMapping(value = "api/v1/events/{eventId}/shift-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SiftPlanCollectionEndpoint {
    private final ShiftPlanService shiftPlanService;

    @GetMapping()
    // TODO: Security
    @Operation(
        operationId = "getAllshiftPlans",
        description = "Find all (volunteer related) shiftPlans"
    )
    public Collection<ShiftPlanDto> getAllShiftPlans(@PathVariable String eventId) throws NotFoundException {
        return shiftPlanService.getAll(ConvertUtil.idToLong(eventId));
    }
    //Todo: Add search capability in future

    @PostMapping()
    // TODO: Security
    @Operation(
        operationId = "createShiftPlan",
        description = "Create a new shiftPlan"
    )
    public ShiftPlanDto createShiftPlan(@PathVariable String eventId, @Valid @RequestBody ShiftPlanModificationDto modificationDto) throws NotFoundException {
        return shiftPlanService.createShiftPlan(ConvertUtil.idToLong(eventId), modificationDto);
    }
}
