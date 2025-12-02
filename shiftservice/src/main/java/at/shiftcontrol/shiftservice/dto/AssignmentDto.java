package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssignmentDto {
    @NotNull
    private PositionSlotDto positionSlot;

    @NotNull
    private VolunteerDto assignedVolunteer;
}
