package at.shiftcontrol.shiftservice.endpoint.user;

import java.util.Collection;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events/{eventId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserAdministrationCollectionEndpoint {
    private final UserAdministrationService service;

    @GetMapping()
    @Operation(
        operationId = "getAllUsersForEvent",
        description = "Find all users for an event"
    )
    public Collection<UserEventDto> getAllUsersForEvent(@PathVariable String eventId) {
        return service.getAllUsersForEvent(ConvertUtil.idToLong(eventId));
    }

    //todo add bulk endpoint
}
