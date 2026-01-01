package at.shiftcontrol.shiftservice.endpoint.shiftplan;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanModificationDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanPatchStatusDto;
import at.shiftcontrol.shiftservice.service.DashboardService;
import at.shiftcontrol.shiftservice.service.ShiftPlanService;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftPlanItemEndpoint {
    private final ShiftPlanService shiftPlanService;
    private final DashboardService dashboardService;

    @GetMapping()
    // TODO: Security
    @Operation(
        operationId = "getShiftPlan",
        description = "Find all (volunteer related) shiftPlans"
    )
    public ShiftPlanDto getAllShiftPlans(@PathVariable String shiftPlanId) throws NotFoundException {
        return shiftPlanService.get(ConvertUtil.idToLong(shiftPlanId));
    }

    @PutMapping()
    // TODO: Security
    @Operation(
        operationId = "updateShiftPlan",
        description = "Update an existing shiftPlan"
    )
    public ShiftPlanDto updateShiftPlan(
        @PathVariable String shiftPlanId,
        @Valid @RequestBody ShiftPlanModificationDto modificationDto
    ) throws NotFoundException {
        return shiftPlanService.update(ConvertUtil.idToLong(shiftPlanId), modificationDto);
    }

    @DeleteMapping()
    // TODO: Security
    @Operation(
        operationId = "deleteShiftPlan",
        description = "Delete an existing shiftPlan"
    )
    public void deleteShiftPlan(@PathVariable String shiftPlanId) throws NotFoundException {
        shiftPlanService.delete(ConvertUtil.idToLong(shiftPlanId));
    }

    @GetMapping("/dashboard")
    // TODO Security
    @Operation(
        operationId = "getShiftPlanDashboard",
        description = "Get (volunteer related) dashboard data for a specific shift plan of an event"
    )
    public ShiftPlanDashboardOverviewDto getShiftPlanDashboard(@PathVariable String shiftPlanId) throws NotFoundException, ForbiddenException {
        return dashboardService.getDashboardOverviewOfShiftPlan(ConvertUtil.idToLong(shiftPlanId));
    }

    @PatchMapping("/status")
    // TODO Security
    @Operation(
        operationId = "editLockStatus",
        description = "Edit the LockState for a shift plan"
    )
    @ResponseStatus(NO_CONTENT)
    public void patchState(@PathVariable String shiftPlanId, @Valid @RequestBody ShiftPlanPatchStatusDto requestDto) throws NotFoundException {
        shiftPlanService.updateLockStatus(ConvertUtil.idToLong(shiftPlanId), requestDto.getLockStatus());
    }
}
