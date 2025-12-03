package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.SignupState;

@Data
@Builder
public class PositionSlotDto {
    @NotNull
    private String id;
    @NotNull
    private String associatedShiftId;
    @NotNull
    private RoleDto role;
    private Collection<VolunteerDto> assignedVolunteers;
    @NotNull
    private LocationDto locations;
    @NotNull
    private SignupState signupState;
    private int desiredVolunteerCount;
}
