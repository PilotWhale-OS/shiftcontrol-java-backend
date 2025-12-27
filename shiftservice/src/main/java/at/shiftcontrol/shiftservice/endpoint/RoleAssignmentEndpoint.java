package at.shiftcontrol.shiftservice.endpoint;

import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.dto.role.RoleAssignmentAssignDto;
import at.shiftcontrol.shiftservice.dto.role.RoleAssignmentDto;
import at.shiftcontrol.shiftservice.service.RoleService;

@Slf4j
@RestController
@RequestMapping(
    value = "/api/v1/events/{eventId}/users/{userId}/role-assignments",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class RoleAssignmentEndpoint {
    private final RoleService roleService;

    @GetMapping
    @Operation(
        operationId = "getRoleAssignments",
        description = "Get role assignments of a user for an event"
    )
    public Collection<RoleAssignmentDto> getRoleAssignments(
        @PathVariable String eventId,
        @PathVariable String userId
    ) {
        return roleService.getRoleAssignmentsForUser(Long.valueOf(eventId), userId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        operationId = "createRoleAssignment",
        description = "Create a role assignment for a user in an event"
    )
    public RoleAssignmentDto createRoleAssignment(
        @PathVariable String eventId,
        @PathVariable String userId,
        @RequestBody RoleAssignmentAssignDto roleAssignmentAssign
    ) throws ForbiddenException {
        return roleService.createRoleAssignment(Long.valueOf(eventId), userId, roleAssignmentAssign);
    }

    @DeleteMapping("/{roleAssignmentId}")
    @Operation(
        operationId = "deleteRoleAssignment",
        description = "Delete a role assignment for a user in an event"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoleAssignment(
        @PathVariable String eventId,
        @PathVariable String userId,
        @PathVariable String roleAssignmentId
    ) throws ForbiddenException {
        roleService.deleteRoleAssignment(Long.valueOf(eventId), userId, Long.valueOf(roleAssignmentId));
    }
}
