package at.shiftcontrol.shiftservice.dto.userinvite;

import java.time.Instant;
import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInviteDto {
    @NotNull
    private String id;

    @NotNull
    private String code;

    @NotNull
    private String email;

    private String preferredUsername;

    private String firstName;

    private String lastName;

    private String displayName;

    @NotNull
    private UserInviteStatus status;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant claimedAt;

    private Instant revokedAt;

    private String claimedUserId;

    @Valid
    @Builder.Default
    private Collection<RoleDto> pendingRoles = java.util.List.of();

    @Valid
    @Builder.Default
    private Collection<UserInviteShiftPlanAccessDto> pendingShiftPlanAccesses = java.util.List.of();
}
