package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.type.SignupState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionSlotDto {
    @NotNull
    private String id;
    @NotNull
    private String associatedShiftId;
    @NotNull
    private RoleDto role;
    private Collection<VolunteerDto> assignedVolunteers;
    //Todo: I thought we discussed that we dont need location per position slot? ~Patrick
//     @NotNull
//     private LocationDto locations;
    @NotNull
    private SignupState signupState;
    private int desiredVolunteerCount;
}
