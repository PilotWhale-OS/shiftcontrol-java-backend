package at.shiftcontrol.shiftservice.dto.invite_join;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanJoinRequestDto {
    @NotNull
    private String inviteCode;
}
