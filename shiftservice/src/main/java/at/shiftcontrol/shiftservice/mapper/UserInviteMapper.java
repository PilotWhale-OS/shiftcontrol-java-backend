package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.Comparator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.entity.ExternalIdentity;
import at.shiftcontrol.lib.entity.UserInvite;
import at.shiftcontrol.lib.entity.UserInviteShiftPlanAccess;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteShiftPlanAccessDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserInviteMapper {
    public static UserInviteDto toDto(UserInvite invite) {
        return UserInviteDto.builder()
            .id(String.valueOf(invite.getId()))
            .code(invite.getCode())
            .email(invite.getEmail())
            .preferredUsername(invite.getPreferredUsername())
            .firstName(invite.getFirstName())
            .lastName(invite.getLastName())
            .displayName(invite.getDisplayName())
            .status(invite.getStatus())
            .createdAt(invite.getCreatedAt())
            .expiresAt(invite.getExpiresAt())
            .claimedAt(invite.getClaimedAt())
            .revokedAt(invite.getRevokedAt())
            .claimedUserId(toClaimedUserId(invite))
            .pendingRoles(RoleMapper.toRoleDto(invite.getPendingRoles()))
            .pendingShiftPlanAccesses(toShiftPlanAccessDtos(invite.getPendingShiftPlanAccesses()))
            .build();
    }

    public static Collection<UserInviteShiftPlanAccessDto> toShiftPlanAccessDtos(Collection<UserInviteShiftPlanAccess> accesses) {
        if (accesses == null) {
            return java.util.List.of();
        }

        return accesses.stream()
            .map(UserInviteMapper::toDto)
            .toList();
    }

    public static UserInviteShiftPlanAccessDto toDto(UserInviteShiftPlanAccess access) {
        return UserInviteShiftPlanAccessDto.builder()
            .shiftPlanId(String.valueOf(access.getShiftPlan().getId()))
            .shiftPlanName(access.getShiftPlan().getName())
            .accessType(access.getAccessType())
            .build();
    }

    private static String toClaimedUserId(UserInvite invite) {
        if (invite.getClaimedUserAccount() == null || invite.getClaimedUserAccount().getExternalIdentities() == null) {
            return null;
        }

        return invite.getClaimedUserAccount().getExternalIdentities().stream()
            .max(Comparator.comparing(ExternalIdentity::getLastSeenAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(ExternalIdentity::getSubject)
            .orElse(null);
    }
}
