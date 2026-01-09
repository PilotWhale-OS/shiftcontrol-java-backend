package at.shiftcontrol.shiftservice.endpoint.user;

import java.util.Collection;

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

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events/{eventId}/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserAdministrationItemEndpoint {
    private final UserAdministrationService service;

    @GetMapping()
    @Operation(
        operationId = "getUserForEvent",
        description = "Find  users for an event"
    )
    public UserEventDto getUserForEvent(@PathVariable String eventId, @PathVariable String userId ) {
        return service.getUserForEvent(ConvertUtil.idToLong(eventId), userId);
    }

    @PatchMapping
    @Operation(
        operationId = "updateUserForEvent",
        description = "Update user plans"
    )
    public Collection<UserEventDto> updateUserForEvent(
        @PathVariable String eventId,
        @PathVariable String userId,
        @RequestBody UserEventUpdateDto updateDto) {
        return service.updateUserForEvent(ConvertUtil.idToLong(eventId), userId, updateDto);
    }
}
