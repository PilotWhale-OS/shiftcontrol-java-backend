package at.shiftcontrol.shiftservice.dto.invite_join;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.RoleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftPlanInviteDto {
    @NotNull
    private String code;

    @NotNull
    private ShiftPlanInviteType type;

    private ShiftPlanDto shiftPlanDto;
    private boolean active;
    private Instant expiresAt;
    private Integer maxUses;
    private int usedCount;

    private Collection<RoleDto> autoAssignedRoles;
    
    private Instant createdAt;
    private Instant revokedAt;

}
