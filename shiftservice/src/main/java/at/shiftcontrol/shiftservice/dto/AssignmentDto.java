package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.AssignmentStatus;

@Data
@Builder
public class AssignmentDto {
    @NotNull
    private String positionSlotId;
    @NotNull
    private VolunteerDto assignedVolunteer;
    @NotNull
    private AssignmentStatus status;
}
