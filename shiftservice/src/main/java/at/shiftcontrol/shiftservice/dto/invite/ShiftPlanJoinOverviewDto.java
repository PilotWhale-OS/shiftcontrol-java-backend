package at.shiftcontrol.shiftservice.dto.invite;

import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.type.ShiftPlanInviteType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanJoinOverviewDto {
    @NotNull
    private ShiftPlanDto shiftPlanDto;

    @NotNull
    private int attendingVolunteerCount;

    @NotNull
    private ShiftPlanInviteType inviteType;

    @NotNull
    private boolean joined;
}
 