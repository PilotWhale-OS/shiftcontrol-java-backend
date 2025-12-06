package at.shiftcontrol.shiftservice.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.shiftservice.dto.ShiftDetailsDto;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/shifts/{shiftId}")
@RequiredArgsConstructor
public class ShiftEndpoint {
    @GetMapping("/details")
    // TODO Security
    @Operation(
        operationId = "getShiftDetails",
        description = "Get details for a specific shift of a shift plan"
    )
    public ShiftDetailsDto getShiftDetails(@PathVariable String shiftId) {
        return null; // TODO: implement
    }
}
