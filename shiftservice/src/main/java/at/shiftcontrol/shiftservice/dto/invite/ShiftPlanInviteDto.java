package at.shiftcontrol.shiftservice.dto.invite;

import java.time.Instant;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.role.RoleDto;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftPlanInviteDto {
    @NotNull
    private String code;

    @NotNull
    private ShiftPlanInviteType type;

    @NotNull
    private ShiftPlanDto shiftPlanDto;

    @NotNull
    private boolean active;

    private Instant expiresAt;
    private Integer maxUses;

    @NotNull
    private int usedCount;

    private Collection<RoleDto> autoAssignedRoles;

    @NotNull
    private Instant createdAt;

    private Instant revokedAt;
}
