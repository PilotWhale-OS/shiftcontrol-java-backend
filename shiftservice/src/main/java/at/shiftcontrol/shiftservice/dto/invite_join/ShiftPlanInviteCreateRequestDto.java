package at.shiftcontrol.shiftservice.dto.invite_join;


import java.time.Instant;
import java.util.Collection;

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
public class ShiftPlanInviteCreateRequestDto {
    @NotNull
    private ShiftPlanInviteType type; // PLANNER_JOIN or VOLUNTEER_JOIN

    private Instant expiresAt;

    private Integer maxUses;

    private Collection<Long> autoAssignRoleIds;
}
