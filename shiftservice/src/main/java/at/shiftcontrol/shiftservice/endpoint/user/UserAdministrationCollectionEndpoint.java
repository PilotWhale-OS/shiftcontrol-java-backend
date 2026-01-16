package at.shiftcontrol.shiftservice.endpoint.user;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Tag(
    name = "user-plan-endpoint"
)
@Slf4j
@RestController
@RequestMapping(value = "api/v1/shift-plans/{shiftPlanId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserAdministrationCollectionEndpoint {
    private final UserAdministrationService service;

    @GetMapping()
    @Operation(
        operationId = "getAllUsers",
        description = "Find all users."
    )
    public PaginationDto<UserPlanDto> getAllUsers(@PathVariable String shiftPlanId, @RequestParam int page, @RequestParam int size) {
        return service.getAllPlanUsers(ConvertUtil.idToLong(shiftPlanId), page, size);
    }

    @PatchMapping("/bulk/add")
    @Operation(
        operationId = "lockUseeInPlan",
        description = "Lock a user in a given plan"
    )
    public Collection<UserPlanDto> bulkAddRoles(
        @PathVariable String shiftPlanId,
        @RequestBody @Valid UserPlanBulkDto updateDto) {
        return service.bulkAddRoles(ConvertUtil.idToLong(shiftPlanId), updateDto);
    }

    @PatchMapping("/bulk/remove")
    @Operation(
        operationId = "lockUseeInPlan",
        description = "Lock a user in a given plan"
    )
    public Collection<UserPlanDto> bulkRemoveRoles(
        @PathVariable String shiftPlanId,
        @RequestBody @Valid UserPlanBulkDto updateDto) {
        return service.bulkRemoveRoles(ConvertUtil.idToLong(shiftPlanId), updateDto);
    }
}
