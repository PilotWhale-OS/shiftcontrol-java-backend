package at.shiftcontrol.shiftservice.dto.userinvite;

import jakarta.validation.constraints.NotBlank;
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
public class UserInviteShiftPlanAccessCreateDto {
    @NotBlank
    private String shiftPlanId;

    @NotNull
    private UserInviteShiftPlanAccessType accessType;
}
