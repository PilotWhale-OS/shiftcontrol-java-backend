package at.shiftcontrol.shiftservice.dto;

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

    @NotNull
    private Collection<VolunteerDto> assignedVolunteers;

    private int desiredVolunteerCount;
}
