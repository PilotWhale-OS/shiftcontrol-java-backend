package at.shiftcontrol.shiftservice.endpoint.shift;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftModificationDto;
import at.shiftcontrol.shiftservice.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "shift-endpoint"
)
@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}/shifts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ShiftCollectionEndpoint {
    private final ShiftService shiftService;

    @PostMapping()
    @Operation(
        operationId = "createShift",
        description = "Create a new shift in a specific shift plan"
    )
    public ShiftDto createShift(@PathVariable String shiftPlanId, @RequestBody @Valid ShiftModificationDto shiftModificationDto)
        throws NotFoundException, ForbiddenException {
        return shiftService.createShift(ConvertUtil.idToLong(shiftPlanId), shiftModificationDto);
    }
}
