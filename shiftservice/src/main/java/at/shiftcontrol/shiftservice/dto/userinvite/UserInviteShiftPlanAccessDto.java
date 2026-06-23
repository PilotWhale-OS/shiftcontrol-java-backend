package at.shiftcontrol.shiftservice.dto.userinvite;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.lib.type.UserInviteShiftPlanAccessType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInviteShiftPlanAccessDto {
    @NotNull
    private String shiftPlanId;

    @NotNull
    private String shiftPlanName;

    @NotNull
    private UserInviteShiftPlanAccessType accessType;
}
