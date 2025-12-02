package at.shiftcontrol.shiftservice.dto;

import at.shiftcontrol.shiftservice.type.SignupState;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class PositionSlotDto {
    @NotNull
    private String id;

    @NotNull
    private RoleDto role;

    private Collection<VolunteerDto> assignedVolunteers;

    @NotNull
    private LocationDto locations;

    @NotNull
    private SignupState signupState;

    private int desiredVolunteerCount;
}
