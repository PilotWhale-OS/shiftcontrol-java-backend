package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public UserEventDto getUserForEvent(@PathVariable String userId ) {
        return service.getUser(userId);
    }

    @PatchMapping
    @Operation(
        operationId = "updateUserPlans",
        description = "Update user plans"
    )
    public UserEventDto updateUserPlans(
        @PathVariable String userId,
        @RequestBody UserEventUpdateDto updateDto) {
        return service.updateUser(userId, updateDto);
    }
}
