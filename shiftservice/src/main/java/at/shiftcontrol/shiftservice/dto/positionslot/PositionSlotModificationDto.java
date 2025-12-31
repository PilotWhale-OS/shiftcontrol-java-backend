package at.shiftcontrol.shiftservice.dto.positionslot;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionSlotModificationDto {
    @NotNull
    private String name;

    private String description;

    @NotNull
    private boolean skipAutoAssignment;

    @NotNull
    private String roleId;

    @NotNull
    private int desiredVolunteerCount;

    @NotNull
    private int rewardPoints;
}
