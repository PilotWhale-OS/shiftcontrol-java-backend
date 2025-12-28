package at.shiftcontrol.shiftservice.dto.invite;

import java.time.Instant;
import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftPlanInviteCreateRequestDto {
    @NotNull
    private ShiftPlanInviteType type;

    private Instant expiresAt;

    private Integer maxUses;

    private Collection<Long> autoAssignRoleIds;
}
