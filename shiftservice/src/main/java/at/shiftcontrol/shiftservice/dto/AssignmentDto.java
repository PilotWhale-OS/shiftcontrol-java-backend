package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.dto.UserProfile.VolunteerDto;
import at.shiftcontrol.shiftservice.type.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDto {
    @NotNull
    private String positionSlotId;
    @NotNull
    private VolunteerDto assignedVolunteer;
    @NotNull
    private AssignmentStatus status;
}
