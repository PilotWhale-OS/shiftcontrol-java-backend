package at.shiftcontrol.shiftservice.dto.invite_join;

import java.time.Instant;

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
public class ShiftPlanInviteCreateResponseDto {
    @NotNull
    private String code;

    // convenience for frontend
    @NotNull
    private String joinUrl;

    @NotNull
    private ShiftPlanInviteType type;

    private Instant expiresAt;
    private Integer maxUses;
}
