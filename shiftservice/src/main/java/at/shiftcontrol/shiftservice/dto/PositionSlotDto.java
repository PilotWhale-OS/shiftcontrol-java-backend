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

    private Collection<String> assignedVolunteerUsernames;
    private int desiredVolunteerCount;

    /**
     *  Specific for the current user's signup state for this position slot.
     */
    @NotNull
    private SignupState signupState;
}
