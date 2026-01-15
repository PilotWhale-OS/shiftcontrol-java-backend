package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.user.LockUserDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserAdministrationItemEndpoint {
    private final UserAdministrationService service;

    @GetMapping()
    @Operation(
        operationId = "getUser",
        description = "Get User"
    )
    public UserEventDto getUser(@PathVariable String userId) {
        return service.getUser(userId);
    }

    @PatchMapping
    @Operation(
        operationId = "updateUserPlans",
        description = "Update user plans"
    )
    public UserEventDto updateUserPlans(
        @PathVariable String userId,
        @RequestBody @Valid UserEventUpdateDto updateDto) {
        return service.updateUser(userId, updateDto);
    }

    @PatchMapping("/lock")
    @Operation(
        operationId = "lockUseeInPlan",
        description = "Lock a user in a given plan"
    )
    public UserEventDto lockUserForPlan(
        @PathVariable String userId,
        @RequestBody @Valid LockUserDto updateDto) {
        return service.lockUser(userId, ConvertUtil.idToLong(updateDto.getShiftPlanId()));
    }

    @PatchMapping("/unlock")
    @Operation(
        operationId = "unlockUserInPlan",
        description = "Unlock a user in a given plan"
    )
    public UserEventDto unLockUserForPlan(
        @PathVariable String userId,
        @RequestBody @Valid LockUserDto updateDto) {
        return service.unLockuser(userId, ConvertUtil.idToLong(updateDto.getShiftPlanId()));
    }
}
