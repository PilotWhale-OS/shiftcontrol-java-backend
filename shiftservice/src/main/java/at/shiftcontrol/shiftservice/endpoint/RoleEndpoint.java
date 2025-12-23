package at.shiftcontrol.shiftservice.endpoint;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.roles.RoleDto;
import at.shiftcontrol.shiftservice.dto.roles.RoleModificationDto;

@Slf4j
@RestController
@RequestMapping(
    value = "/api/v1/events/{eventId}/roles",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class RoleEndpoint {
    @GetMapping
    // TODO Security
    @Operation(
        operationId = "getRoles",
        description = "Get roles of this event"
    )
    public List<RoleDto> getRoles(@PathVariable String eventId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    // TODO Security
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "createRole",
        description = "Create a new role for an event"
    )
    public RoleDto createRole(
        @PathVariable String eventId,
        @RequestBody RoleModificationDto role
    ) throws NotFoundException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PutMapping(value = "/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    // TODO Security
    @Operation(
        operationId = "updateRole",
        description = "Update a role of this event"
    )
    public RoleDto updateRole(
        @PathVariable String eventId,
        @PathVariable String roleId,
        @RequestBody RoleModificationDto role
    ) throws NotFoundException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @DeleteMapping("/{roleId}")
    // TODO Security
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        operationId = "deleteRole",
        description = "Delete a role of this event"
    )
    public void deleteRole(
        @PathVariable String eventId,
        @PathVariable String roleId
    ) throws NotFoundException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
