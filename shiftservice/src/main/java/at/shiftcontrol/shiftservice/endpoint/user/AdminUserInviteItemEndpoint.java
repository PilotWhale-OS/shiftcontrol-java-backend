package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteAdministrationService;

@Tag(name = "user-invite-endpoint")
@RestController
@RequestMapping(value = "api/v1/users/invites/{inviteId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminUserInviteItemEndpoint {
    private final UserInviteAdministrationService service;

    @PostMapping("/revoke")
    @Operation(
        operationId = "revokeUserInvite",
        description = "Revoke a pending user invite."
    )
    public void revokeInvite(@PathVariable String inviteId) {
        service.revokeInvite(ConvertUtil.idToLong(inviteId));
    }
}
