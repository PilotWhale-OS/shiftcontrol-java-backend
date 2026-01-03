package at.shiftcontrol.shiftservice.endpoint.role;

import java.util.Collection;

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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.dto.role.RoleModificationDto;
import at.shiftcontrol.shiftservice.service.role.RoleService;

@Slf4j
@RestController
@RequestMapping(
    value = "/api/v1/shift-plans/{shiftPlanId}/roles",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class RoleEndpoint {
    private final RoleService roleService;

    @GetMapping
    // TODO Security
    @Operation(
        operationId = "getRoles",
        description = "Get roles of this event"
    )
    public Collection<RoleDto> getRoles(@PathVariable String shiftPlanId) {
        return roleService.getRoles(ConvertUtil.idToLong(shiftPlanId));
    }

    @GetMapping("/{roleId}")
    // TODO Security
    @Operation(
        operationId = "getRole",
        description = "Get role by id"
    )
    public RoleDto getRole(@PathVariable String shiftPlanId, @PathVariable String roleId) throws ForbiddenException {
        return roleService.getRole(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(roleId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    // TODO Security
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        operationId = "createRole",
        description = "Create a new role for an event"
    )
    public RoleDto createRole(
        @PathVariable String shiftPlanId,
        @RequestBody @Valid RoleModificationDto role
    ) throws ForbiddenException {
        return roleService.createRole(ConvertUtil.idToLong(shiftPlanId), role);
    }

    @PutMapping(value = "/{roleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    // TODO Security
    @Operation(
        operationId = "updateRole",
        description = "Update a role of this event"
    )
    public RoleDto updateRole(
        @PathVariable String shiftPlanId,
        @PathVariable String roleId,
        @RequestBody @Valid RoleModificationDto role
    ) throws ForbiddenException {
        return roleService.updateRole(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(roleId), role);
    }

    @DeleteMapping("/{roleId}")
    // TODO Security
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        operationId = "deleteRole",
        description = "Delete a role of this event"
    )
    public void deleteRole(
        @PathVariable String shiftPlanId,
        @PathVariable String roleId
    ) throws ForbiddenException {
        roleService.deleteRole(ConvertUtil.idToLong(shiftPlanId), ConvertUtil.idToLong(roleId));
    }
}
