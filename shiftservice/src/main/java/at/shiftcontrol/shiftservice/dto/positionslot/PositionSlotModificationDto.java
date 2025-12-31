package at.shiftcontrol.shiftservice.dto.positionslot;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255)
    private String name;

    @Size(max = 1024)
    private String description;

    @NotNull
    private boolean skipAutoAssignment;

    @NotNull
    private String roleId;

    @NotNull
    @Min(0)
    private int desiredVolunteerCount;

    @NotNull
    @Min(0)
    private int rewardPoints;
}
