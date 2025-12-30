package at.shiftcontrol.shiftservice.mapper;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;

import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.entity.Event;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class InviteMapper {
    public static ShiftPlanInviteDto toInviteDto(ShiftPlanInvite invite, ShiftPlan shiftPlan) {
        return ShiftPlanInviteDto.builder()
            .code(invite.getCode())
            .type(invite.getType())
            .shiftPlanDto(ShiftPlanMapper.toShiftPlanDto(shiftPlan))
            .active(invite.isActive())
            .expiresAt(invite.getExpiresAt())
            .maxUses(invite.getMaxUses())
            .usedCount(invite.getUses())
            .autoAssignedRoles(invite.getAutoAssignRoles() == null ? null : RoleMapper.toRoleDto(invite.getAutoAssignRoles()))
            .createdAt(invite.getCreatedAt())
            .revokedAt(invite.getRevokedAt())
            .build();
    }
}
