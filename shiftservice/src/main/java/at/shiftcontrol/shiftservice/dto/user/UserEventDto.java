package at.shiftcontrol.shiftservice.dto.user;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventDto {
    @NotNull
    @Valid
    private VolunteerDto volunteer;

    @NotNull
    private String email;

    @NotNull
    private Collection<String> planningPlans;

    @NotNull
    private Collection<String> lockedPlans;

    @NotNull
    private Collection<String> volunteeringPlans;
}
